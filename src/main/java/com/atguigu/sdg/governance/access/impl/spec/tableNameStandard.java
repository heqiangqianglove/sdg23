package com.atguigu.sdg.governance.access.impl.spec;

import com.atguigu.sdg.governance.access.Access;
import com.atguigu.sdg.governance.bean.GovernanceAssessDetail;
import com.atguigu.sdg.governance.bean.InputDetail;
import com.atguigu.sdg.meta.constant.MetaConst;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 规范   判断表明是否合规
 */
@Component("TABLE_NAME_STANDARD")
public class tableNameStandard extends Access {
    Pattern odsPattern = Pattern.compile("^ods_.+_(inc|full)$");
    Pattern dwdPattern =Pattern.compile("^dwd_.+_.+_(inc|full)$");
    Pattern dimPattern =Pattern.compile("^dim_.+_(zip|full)$");
    Pattern dwsPattern =Pattern.compile("^dws_.+_.+_.+_(\\d+d|td)$");
    Pattern adsPattern =Pattern.compile("^ads_.+");
    Pattern dmPattern =Pattern.compile("^dm_.+");

    @Override
    protected void access(InputDetail inputDetail, GovernanceAssessDetail governanceAssessDetail) throws ParseException {
        //获取层级和表名
        String tableName = inputDetail.getTableMetaInfo().getTableName();
        String dwLevel = inputDetail.getTableMetaInfo().getTableMetaInfoExtra().getDwLevel();
        //判断层级，如果是其它类型，则给5分返回
        if(dwLevel.equals(MetaConst.DW_LEVEL_OTHER)){
            governanceAssessDetail.setAssessScore(BigDecimal.valueOf(5));
            governanceAssessDetail.setAssessProblem("层级类型未设置");
            governanceAssessDetail.setGovernanceUrl(inputDetail.getGovernanceMetric().getGovernanceUrl()+inputDetail.getTableMetaInfo().getId());
            return;
        }
        //根据层级名去匹配对应的正则
        Pattern pattern = null;
        switch (dwLevel){
            case "ODS":pattern=odsPattern;break;
            case "DWD":pattern=dwdPattern;break;
            case "DIM":pattern=dimPattern;break;
            case "DWS":pattern=dwsPattern;break;
            case "ADS":pattern=adsPattern;break;
            case "DM":pattern=dmPattern;break;
        }
        //根据表明进行匹配
        Matcher matcher = pattern.matcher(tableName);
        if(!matcher.matches()){
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("表名不符合规范");
        }

    }
}
