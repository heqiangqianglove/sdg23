package com.atguigu.sdg.meta.mapper;

import com.atguigu.sdg.meta.bean.TableMetaInfo;
import com.atguigu.sdg.meta.bean.TableMetaInfoVO;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 元数据表 Mapper 接口
 * </p>
 *
 * @author zhangjunyi
 * @since 2023-06-04
 */
@Mapper
@DS("dga")
public interface TableMetaInfoMapper extends BaseMapper<TableMetaInfo> {

    @Select("${sql}")
    List<TableMetaInfoVO> getTableList(@Param("sql") String sql);
    @Select("${sql}")
    Integer getCount(@Param("sql") String sql);

    @Select("select tm.id as tm_id , \n" +
            "        tm.schema_name as tm_schema_name,  \n" +
            "        tm.table_name as tm_table_name,\n" +
            "        tm.create_time as tm_create_time,\n" +
            "        tm.update_time as tm_update_time,\n" +
            "         te.id as te_id ,\n" +
            "        te.schema_name as te_schema_name ,\n" +
            "        te.table_name as te_table_name,\n" +
            "        te.create_time as te_create_time,\n" +
            "        te.update_time as te_update_time,\n" +
            "            tm.* ,te.* from  table_meta_info tm join table_meta_info_extra te\n" +
            "         on tm.table_name=te.table_name and tm.schema_name=te.schema_name \n" +
            "         where assess_date= (select max(assess_date)  from table_meta_info)")
    @ResultMap("TableMetaInfoMap")
    List<TableMetaInfo> getTableMetaList();
}
