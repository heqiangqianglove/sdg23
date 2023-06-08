package com.atguigu.sdg.governance.access;

import com.atguigu.sdg.governance.bean.GovernanceAssessDetail;
import com.atguigu.sdg.governance.bean.InputDetail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

public abstract class Access {
     public final GovernanceAssessDetail mainAccess(InputDetail inputDetail){
         GovernanceAssessDetail governanceAssessDetail=new GovernanceAssessDetail();
         governanceAssessDetail.setAssessDate(inputDetail.getAccessDate());
         governanceAssessDetail.setTableName(inputDetail.getTableMetaInfo().getTableName());
         governanceAssessDetail.setSchemaName(inputDetail.getTableMetaInfo().getSchemaName());
         governanceAssessDetail.setMetricId(inputDetail.getGovernanceMetric().getId()+"");
         governanceAssessDetail.setMetricName(inputDetail.getGovernanceMetric().getMetricName());
         governanceAssessDetail.setGovernanceType(inputDetail.getGovernanceMetric().getGovernanceType());
         governanceAssessDetail.setTecOwner(inputDetail.getTableMetaInfo().getTableMetaInfoExtra().getTecOwnerUserName());
         governanceAssessDetail.setAssessScore(BigDecimal.TEN);
         try {
             access(inputDetail,governanceAssessDetail);
         }catch (Exception e){
             /**
              * 这段代码创建了一个StringWriter对象和一个PrintWriter对象。其中，StringWriter是一个字符流，可以用来写入字符串，而PrintWriter是一个字符输出流，可以将字符打印到指定的输出流中。
              *
              * 然后，代码调用了异常对象e的printStackTrace方法，并将printWriter对象作为参数传递进去。这样，异常堆栈信息就会被写入到printWriter所关联的StringWriter中。
              *
              * 最终，我们可以通过调用stringWriter.toString()方法，将异常堆栈信息以字符串的形式获取到，以便于后续处理。
              */
             //获取前两千条打印信息到 governanceAssessDetail.setAssessExceptionMsg()中
             StringWriter stringWriter=new StringWriter();
             PrintWriter printWriter=new PrintWriter(stringWriter);
             e.printStackTrace(printWriter);
             governanceAssessDetail.setIsAssessException("1");
             int num=stringWriter.toString().length();
             governanceAssessDetail.setAssessExceptionMsg(stringWriter.toString().substring(0,Math.min(num,2000)));
         }
         //设置创建日期
         governanceAssessDetail.setCreateTime(new Date());
        return governanceAssessDetail;
    }
    protected abstract void access(InputDetail inputDetail,GovernanceAssessDetail governanceAssessDetail) throws ParseException;
}
