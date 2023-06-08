package com.atguigu.sdg.meta.service;

import com.atguigu.sdg.meta.bean.TableMetaInfo;
import com.atguigu.sdg.meta.bean.TableMetaInfoVO;
import com.atguigu.sdg.meta.bean.TableReqInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 元数据表 服务类
 * </p>
 *
 * @author zhangjunyi
 * @since 2023-06-04
 */
public interface TableMetaInfoService extends IService<TableMetaInfo> {
    public void initMetaInfo(String date, String dataBaseName);

    List<TableMetaInfoVO> getTableList(TableReqInfo tableReqInfo);

    Integer getCount(TableReqInfo tableReqInfo);

    List<TableMetaInfo> getTableMetaList();
}
