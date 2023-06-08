package com.atguigu.sdg.meta.service.impl;

import com.atguigu.sdg.meta.bean.TableMetaInfo;
import com.atguigu.sdg.meta.bean.TableMetaInfoExtra;
import com.atguigu.sdg.meta.constant.MetaConst;
import com.atguigu.sdg.meta.mapper.TableMetaInfoExtraMapper;
import com.atguigu.sdg.meta.service.TableMetaInfoExtraService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 元数据表附加信息 服务实现类
 * </p>
 *
 * @author heqiangqiang
 * @since 2023-06-05
 */
@Service
@DS("dga")
public class TableMetaInfoExtraServiceImpl extends ServiceImpl<TableMetaInfoExtraMapper, TableMetaInfoExtra> implements TableMetaInfoExtraService {

    @Override
    public void initMetaInfoExtra(List<TableMetaInfo> tableMetaInfoList) {
        //创建List集合，方便进行批次添加
        List<TableMetaInfoExtra> list=new ArrayList<>();
        //遍历对应的表
        for (TableMetaInfo tableMetaInfo : tableMetaInfoList) {
            //根据表信息查看是否数据已存在
            TableMetaInfoExtra one = getOne(new QueryWrapper<TableMetaInfoExtra>().eq("table_name", tableMetaInfo.getTableName()).eq("schema_name", tableMetaInfo.getSchemaName()));
            //如果不存在则进行添加
            if(one==null){
                TableMetaInfoExtra tableMetaInfoExtra=new TableMetaInfoExtra();
                //设置表明
                tableMetaInfoExtra.setTableName(tableMetaInfo.getTableName());
                //设置库名
                tableMetaInfoExtra.setSchemaName(tableMetaInfo.getSchemaName());
                //设置存储周期类型
                tableMetaInfoExtra.setLifecycleType(MetaConst.LIFECYCLE_TYPE_UNSET);
                //设置安全级别
                tableMetaInfoExtra.setSecurityLevel(MetaConst.SECURITY_LEVEL_UNSET);
                //设置数仓所在层级
                tableMetaInfoExtra.setDwLevel(DwLevel(tableMetaInfo.getTableName()));
                //设置创建时间
                tableMetaInfoExtra.setCreateTime(new Date());
                //添加到集合中
                list.add(tableMetaInfoExtra);
            }
        }
        //批量保存
        saveOrUpdateBatch(list,500);
    }

    private String DwLevel(String tableName) {
        if(tableName.startsWith("ods")){
            return MetaConst.DW_LEVEL_ODS;
        }else
        if(tableName.startsWith("dwd")){
            return MetaConst.DW_LEVEL_DWD;
        }else
        if(tableName.startsWith("dws")){
            return MetaConst.DW_LEVEL_DWS;
        }else
        if(tableName.startsWith("dim")){
            return MetaConst.DW_LEVEL_DIM;
        }else
        if(tableName.startsWith("ads")){
            return MetaConst.DW_LEVEL_ADS;
        }else {
            return MetaConst.DW_LEVEL_OTHER;
        }
    }
}
