package com.atguigu.sdg.governance.service;

import com.atguigu.sdg.governance.bean.GovernanceAssessDetail;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 治理考评结果明细 服务类
 * </p>
 *
 * @author heqiangqiang
 * @since 2023-06-06
 */
@DS("dga")
public interface GovernanceAssessDetailService extends IService<GovernanceAssessDetail> {
    void setGovernanceDetail(String accessDate);
}
