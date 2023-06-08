package com.atguigu.sdg.meta.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.atguigu.sdg.meta.bean.TableMetaInfo;
import com.atguigu.sdg.meta.bean.TableMetaInfoVO;
import com.atguigu.sdg.meta.bean.TableReqInfo;
import com.atguigu.sdg.meta.mapper.TableMetaInfoMapper;
import com.atguigu.sdg.meta.service.TableMetaInfoExtraService;
import com.atguigu.sdg.meta.service.TableMetaInfoService;
import com.atguigu.sdg.meta.utils.SqlUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.hadoop.hive.metastore.conf.MetastoreConf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 元数据表 服务实现类
 * </p>
 *
 * @author 何强强
 * @since 2023-06-04
 */
@Service
@DS("dga")
public class TableMetaInfoServiceImpl extends ServiceImpl<TableMetaInfoMapper, TableMetaInfo> implements TableMetaInfoService {
    @Autowired
    TableMetaInfoExtraService tableMetaInfoExtraService;
    @Autowired
    private TableMetaInfoMapper tableMetaInfoMapper;
    @Value("${hive.meta-server.url}")
    String  hiveClientUrl;
    @Override
    public void initMetaInfo(String date, String dataBaseName) {
        //移除之前已经存在的数据，防止重复调用
        remove(new QueryWrapper<TableMetaInfo>().eq("assess_date",date));
        //获取到hive和hdfs元数据的所有TableMetaInfo信息
        List<TableMetaInfo> list=extract(date,dataBaseName);
        //将数据保存到表中
        saveOrUpdateBatch(list);
        //调用TableMetaInfoService中的方法
        tableMetaInfoExtraService.initMetaInfoExtra(list);
    }

    @Override
    public List<TableMetaInfoVO> getTableList(TableReqInfo tableReqInfo) {
        StringBuilder sb=new StringBuilder();
        sb.append("select \n" +
                " tm.id,\n" +
                " tm.table_name,\n" +
                " tm.schema_name,\n" +
                " tm.table_size,\n" +
                " tm.table_comment,\n" +
                " te.tec_owner_user_name,\n" +
                " te.busi_owner_user_name,\n" +
                " tm.table_last_modify_time,\n" +
                " tm.table_last_access_time\n" +
                " from dga_230201.table_meta_info tm\n" +
                " JOIN dga_230201.table_meta_info_extra te \n" +
                " on tm.schema_name=te.schema_name and tm.table_name=te.table_name");
        sb.append(" where assess_date=(select max(assess_date) from table_meta_info)");
        if(tableReqInfo.getSchemaName()!=null)
            sb.append(" and tm.schema_name like '%"+ SqlUtil.filterUnsafeSql(tableReqInfo.getSchemaName())+"%'");
        if(tableReqInfo.getTableName()!=null)
            sb.append(" and tm.table_name like '%"+SqlUtil.filterUnsafeSql(tableReqInfo.getTableName())+"%'");
        if(tableReqInfo.getDwLevel()!=null)
            sb.append(" and dw_level like '%"+SqlUtil.filterUnsafeSql(tableReqInfo.getDwLevel())+"%'");
        //计算当前页开始的位置
        int num=(tableReqInfo.getPageNo()-1)*tableReqInfo.getPageSize();
        //分页
        sb.append(" limit "+num+","+tableReqInfo.getPageSize());
        //调用mapper层，根据条件分页查询数据
        return tableMetaInfoMapper.getTableList(sb.toString());
    }

    @Override
    public Integer getCount(TableReqInfo tableReqInfo) {
        StringBuilder sb=new StringBuilder();
        sb.append("select \n" +
                " count(*)\n" +
                " from dga_230201.table_meta_info tm\n" +
                " JOIN dga_230201.table_meta_info_extra te \n" +
                " on tm.schema_name=te.schema_name and tm.table_name=te.table_name");
        if(tableReqInfo.getSchemaName()!=null)
            sb.append(" where tm.schema_name like '%"+SqlUtil.filterUnsafeSql(tableReqInfo.getSchemaName())+"%'");
        if(tableReqInfo.getTableName()!=null)
            sb.append(" and tm.table_name like '%"+SqlUtil.filterUnsafeSql(tableReqInfo.getTableName())+"%'");
        if(tableReqInfo.getDwLevel()!=null)
            sb.append(" and dw_level like '%"+SqlUtil.filterUnsafeSql(tableReqInfo.getDwLevel())+"%'");
        return tableMetaInfoMapper.getCount(sb.toString());
    }

    @Override
    public List<TableMetaInfo> getTableMetaList() {
        return tableMetaInfoMapper.getTableMetaList();
    }

    /**
     * 获取对应的hive和hdfs信息
     * @param date
     * @param dataBaseName
     * @return
     */
    private List<TableMetaInfo> extract(String date, String dataBaseName) {
        List<TableMetaInfo> list=null;
        try {

            //获取元数据中所有的表名
            List<String> allTables = getHiveClient().getAllTables(dataBaseName);
            //设置集合预估容量，防止扩容浪费空间(优化项）
            list=new ArrayList<>(allTables.size());
            //遍历表名，并从hive和hdfs中获得TableMetaInfo的信息
            for (String allTable : allTables) {
                //根据表名获取hive中的信息
                TableMetaInfo tableMetaInfo=getHiveInfo(dataBaseName,allTable);
                getHdfsInfo(tableMetaInfo);
                //设置考评日期
                tableMetaInfo.setAssessDate(date);
                //设置创建时间
                tableMetaInfo.setCreateTime(new Date());
                list.add(tableMetaInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取hdfs的信息
     * @param tableMetaInfo
     */
    private void getHdfsInfo(TableMetaInfo tableMetaInfo) {
        try {
            //获取hdfs连接
            FileSystem fileSystem = FileSystem.get(new URI(tableMetaInfo.getTableFsPath()), new Configuration(), tableMetaInfo.getTableFsOwner());
            //获取表所在的目录
            FileStatus[] fileStatuses = fileSystem.listStatus(new Path(tableMetaInfo.getTableFsPath()));
            //获取文件内容
            getFileInfo(fileStatuses,fileSystem,tableMetaInfo);
            tableMetaInfo.setFsCapcitySize(fileSystem.getStatus().getCapacity());
            tableMetaInfo.setFsUsedSize(fileSystem.getStatus().getUsed());
            tableMetaInfo.setFsRemainSize(fileSystem.getStatus().getRemaining());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 获取文件的内容
     * @param fileStatuses
     */
    private void getFileInfo(FileStatus[] fileStatuses,FileSystem fileSystem,TableMetaInfo tableMetaInfo) throws IOException {
        for (FileStatus fileStatus : fileStatuses) {
            if(fileStatus.isFile()){
                //为了防止空指针异常，需要判断是否首次设置数据大小（设置数据量大小）
                if(tableMetaInfo.getTableSize()==null) {
                    tableMetaInfo.setTableSize(fileStatus.getLen());
                }else {
                    tableMetaInfo.setTableSize(tableMetaInfo.getTableSize()+fileStatus.getLen());
                }
                //设置所有副本数据量总大小
                if(tableMetaInfo.getTableTotalSize()==null){
                    tableMetaInfo.setTableTotalSize(fileStatus.getReplication()*fileStatus.getLen());
                }else {
                    tableMetaInfo.setTableTotalSize(tableMetaInfo.getTableTotalSize()+fileStatus.getReplication()*fileStatus.getLen());
                }
                //设置最后修改时间
                Date modTime=new Date(fileStatus.getModificationTime());
                Date accTime=new Date(fileStatus.getAccessTime());
                //进行判空(modTime)
                if(tableMetaInfo.getTableLastModifyTime()==null){
                    tableMetaInfo.setTableLastModifyTime(modTime);
                }else {
                    //比较大小，只要最新的修改时间
                    if (tableMetaInfo.getTableLastModifyTime().getTime()<fileStatus.getModificationTime()){
                        tableMetaInfo.setTableLastModifyTime(modTime);
                    }
                }
                //进行判空(accTime)
                if(tableMetaInfo.getTableLastAccessTime()==null){
                    tableMetaInfo.setTableLastAccessTime(accTime);
                }else {
                    //比较大小，只要最新的修改时间
                    if (tableMetaInfo.getTableLastAccessTime().getTime()<fileStatus.getAccessTime()){
                        tableMetaInfo.setTableLastAccessTime(accTime);
                    }
                }
            }else {
                //递归调用
                FileStatus[] fileStatuses1 = fileSystem.listStatus(fileStatus.getPath());
                getFileInfo(fileStatuses1,fileSystem,tableMetaInfo);
            }
        }
    }

    /**
     * 拿到hive对应的信息数据
     * @param dataBaseName
     * @param allTable
     * @return
     */
    private TableMetaInfo getHiveInfo(String dataBaseName, String allTable) {
        TableMetaInfo tableMetaInfo=null;
        try {
            Table table = getHiveClient().getTable(dataBaseName, allTable);
            tableMetaInfo=new TableMetaInfo();
            //设置表名、库名
            tableMetaInfo.setTableName(allTable);
            tableMetaInfo.setSchemaName(dataBaseName);
            //设置字段名
            PropertyPreFilters.MySimplePropertyPreFilter propertyPreFilter=new PropertyPreFilters().addFilter("name","type","comment");
            tableMetaInfo.setColNameJson(JSONObject.toJSONString(table.getSd().getCols(),propertyPreFilter));
            //设置分区字段名
            tableMetaInfo.setPartitionColNameJson(JSONObject.toJSONString(table.getPartitionKeys(),propertyPreFilter));
            //设置hdfs所输人
            tableMetaInfo.setTableFsOwner(table.getOwner());
            //设置参数信息
            tableMetaInfo.setTableParametersJson(JSONObject.toJSONString(table.getParameters()));
            //设置表备注
            tableMetaInfo.setTableComment(table.getParameters().get("comment"));
            //设置hdfs路径
            tableMetaInfo.setTableFsPath(table.getSd().getLocation());
            //设置输入\输出格式
            tableMetaInfo.setTableInputFormat(table.getSd().getInputFormat());
            tableMetaInfo.setTableOutputFormat(table.getSd().getOutputFormat());
            //设置行格式
            tableMetaInfo.setTableRowFormatSerde(table.getSd().getSerdeInfo().getSerializationLib());
            //设置表创建时间
            String dateFormat= DateFormatUtils.format(new Date(table.getCreateTime()*1000L),"yyyy-MM-dd HH:ss:mm");
            tableMetaInfo.setTableCreateTime(dateFormat);
            //表类型
            tableMetaInfo.setTableType(table.getTableType());
            //分桶列，判断有没有分桶，没有不添加
            if(table.getSd().getBucketCols().size()>0){
                tableMetaInfo.setTableBucketColsJson(JSONObject.toJSONString(table.getSd().getBucketCols()));
                tableMetaInfo.setTableBucketNum(table.getSd().getNumBuckets()+0L);
                tableMetaInfo.setTableSortColsJson(JSONObject.toJSONString(table.getSd().getSortCols()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableMetaInfo;
    }

    // 初始化 hive 客户端
    private IMetaStoreClient getHiveClient() {
        HiveConf hiveConf = new HiveConf();
        MetastoreConf.setVar(hiveConf, MetastoreConf.ConfVars.THRIFT_URIS, hiveClientUrl);
        try {
            return new HiveMetaStoreClient(hiveConf);
        } catch (MetaException e) {
            throw new RuntimeException(e);
        }

    }
}
