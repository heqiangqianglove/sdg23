package com.atguigu.sdg.governance.access.impl.spec;

import com.atguigu.sdg.governance.access.Access;
import com.atguigu.sdg.governance.bean.GovernanceAssessDetail;
import com.atguigu.sdg.governance.bean.InputDetail;
import com.atguigu.sdg.meta.bean.TableMetaInfo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;

@Component("TABLE_COMMENT")
public class tableComment extends Access {
    @Override
    protected void access(InputDetail inputDetail, GovernanceAssessDetail governanceAssessDetail) throws ParseException {
        TableMetaInfo tableMetaInfo = inputDetail.getTableMetaInfo();
        //判断是否有表备注
        if(tableMetaInfo.getTableComment()==null&&tableMetaInfo.getTableComment().length()<=0){
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("缺少表备注");
        }
    }
}
