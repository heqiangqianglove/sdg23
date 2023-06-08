package com.atguigu.sdg.governance.access.impl.cacl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.sdg.governance.access.Access;
import com.atguigu.sdg.governance.bean.GovernanceAssessDetail;
import com.atguigu.sdg.governance.bean.InputDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 计算  --- 长期无访问
 */
@Component("NO_ACCESS")
public class noAccess extends Access {
    @Override
    protected void access(InputDetail inputDetail, GovernanceAssessDetail governanceAssessDetail) throws ParseException {
        //获取当前考评日期（1）由于考评的是最近访问天数，需要将日期量化到天
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String accessDate = inputDetail.getAccessDate();
        //转换成日期类型
        Date parse = simpleDateFormat.parse(accessDate);
        long timeCurrent = parse.getTime();

        //获取最后一次表的访问时间
        Date tableLastAccessTime = inputDetail.getTableMetaInfo().getTableLastAccessTime();
        //先进行日期量化
        String format = simpleDateFormat.format(tableLastAccessTime);
        //转换回日期类型
        Date parse1 = simpleDateFormat.parse(format);
        long timeLastAccess = parse1.getTime();

        //两者相减,得到天数
        long l = (timeCurrent - timeLastAccess) / (1000 * 60 * 60 * 24);

        //获取访问最低day
        String metricParamsJson = inputDetail.getGovernanceMetric().getMetricParamsJson();
        JSONObject jsonObject = JSON.parseObject(metricParamsJson);
        Long days = jsonObject.getLong("days");

        //判断如果比阈值大，则给0分
        if(l>days){
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("超过"+days+"天没有访问");
        }
    }
}
