package com.atguigu.sdg.governance.access.impl.TecOwner;

import com.atguigu.sdg.governance.access.Access;
import com.atguigu.sdg.governance.bean.GovernanceAssessDetail;
import com.atguigu.sdg.governance.bean.InputDetail;
import com.atguigu.sdg.meta.bean.TableMetaInfoExtra;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 规范  ---  表名是否合规
 */
@Component("TEC_OWNER")
public class tecOwner extends Access {
    @Override
    protected void access(InputDetail inputDetail, GovernanceAssessDetail governanceAssessDetail) {
        TableMetaInfoExtra tableMetaInfoExtra = inputDetail.getTableMetaInfo().getTableMetaInfoExtra();
        if(tableMetaInfoExtra.getTecOwnerUserName()==null||tableMetaInfoExtra.getTecOwnerUserName().length()<=0){
            //表示没有设置技术负责人,设置得分为0
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            //设置处理路径
            governanceAssessDetail.setGovernanceUrl(inputDetail.getGovernanceMetric().getGovernanceUrl()+inputDetail.getTableMetaInfo().getId());
            //设置考评问题项
            governanceAssessDetail.setAssessProblem("没有技术负责人");
        }
    }
}
