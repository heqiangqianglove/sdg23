package com.atguigu.sdg.meta.service;

import com.atguigu.sdg.meta.bean.TableMetaInfo;
import com.atguigu.sdg.meta.bean.TableMetaInfoExtra;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 元数据表附加信息 服务类
 * </p>
 *
 * @author heqiangqiang
 * @since 2023-06-05
 */
public interface TableMetaInfoExtraService extends IService<TableMetaInfoExtra> {
    void initMetaInfoExtra(List<TableMetaInfo> tableMetaInfoList);
}
