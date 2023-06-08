package com.atguigu.sdg.meta.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.sdg.meta.bean.TableMetaInfo;
import com.atguigu.sdg.meta.bean.TableMetaInfoExtra;
import com.atguigu.sdg.meta.bean.TableMetaInfoVO;
import com.atguigu.sdg.meta.bean.TableReqInfo;
import com.atguigu.sdg.meta.service.TableMetaInfoExtraService;
import com.atguigu.sdg.meta.service.TableMetaInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 元数据表 前端控制器
 * </p>
 *
 * @author zhangjunyi
 * @since 2023-06-04
 */
@RestController
@RequestMapping("/tableMetaInfo")
public class TableMetaInfoController {
    @Autowired
    private TableMetaInfoService tableMetaInfoService;

    @Autowired
    TableMetaInfoExtraService tableMetaInfoExtraService;
    /**
     * 手动更新元数据
     * @param schemaName
     * @param assesDate
     */
    @PostMapping("/init-tables/{schemaName}/{assesDate}")
    public void initTable(@PathVariable("schemaName")String schemaName,@PathVariable("assesDate")String assesDate){
        tableMetaInfoService.initMetaInfo(assesDate,schemaName);
    }

    /**
     *根据条件分页查询元数据
     */
     @GetMapping("/table-list")
    public JSONObject getTableList(TableReqInfo tableReqInfo){
         //根据条件获取结果集合
         List<TableMetaInfoVO> list=tableMetaInfoService.getTableList(tableReqInfo);
         //根据条件获取总条数
         Integer count=tableMetaInfoService.getCount(tableReqInfo);
         JSONObject jsonObject=new JSONObject();
         jsonObject.put("total",count);
         jsonObject.put("list",list);
         return jsonObject;
     }
    /**
     * 根据id获取表的元数据详情信息
     */
    @GetMapping("table/{id}")
    public String getByIdInfo(@PathVariable("id")String id){
        //获取TableMetaInfo信息
        TableMetaInfo byId = tableMetaInfoService.getById(id);
        //设置更新日期
        byId.setUpdateTime(new Date());
        //获取辅助信息
        TableMetaInfoExtra one = tableMetaInfoExtraService.getOne(new QueryWrapper<TableMetaInfoExtra>().eq("table_name", byId.getTableName()).eq("schema_name", byId.getSchemaName()));

        byId.setTableMetaInfoExtra(one);
        return JSONObject.toJSONString(byId);
    }

    /**
     * 根据id修改元数据详情信息
     */
    @PostMapping("tableExtra")
    public void updateByIdInfoExtra(@RequestBody TableMetaInfoExtra tableMetaInfoExtra){
        tableMetaInfoExtra.setUpdateTime(new Date());
        boolean b = tableMetaInfoExtraService.updateById(tableMetaInfoExtra);
    }

}
