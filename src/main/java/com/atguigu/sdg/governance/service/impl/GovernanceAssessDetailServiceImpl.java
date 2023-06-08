package com.atguigu.sdg.governance.service.impl;

import com.atguigu.sdg.governance.access.Access;
import com.atguigu.sdg.governance.bean.GovernanceAssessDetail;
import com.atguigu.sdg.governance.bean.GovernanceMetric;
import com.atguigu.sdg.governance.bean.InputDetail;
import com.atguigu.sdg.governance.mapper.GovernanceAssessDetailMapper;
import com.atguigu.sdg.governance.service.GovernanceAssessDetailService;
import com.atguigu.sdg.governance.service.GovernanceMetricService;
import com.atguigu.sdg.governance.springApplication.springApplication;
import com.atguigu.sdg.meta.bean.TableMetaInfo;
import com.atguigu.sdg.meta.service.TableMetaInfoService;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 治理考评结果明细 服务实现类
 * </p>
 *
 * @author heqiangqiang
 * @since 2023-06-06
 */
@DS("dga")
@Service
public class GovernanceAssessDetailServiceImpl extends ServiceImpl<GovernanceAssessDetailMapper, GovernanceAssessDetail> implements GovernanceAssessDetailService {
    @Autowired
    private TableMetaInfoService tableMetaInfoService;
    @Autowired
    private GovernanceMetricService governanceMetricService;
    @Override
    public void setGovernanceDetail(String accessDate) {
        //获取tableMetaInfo的所有信息
        List<TableMetaInfo> tableMetaList = tableMetaInfoService.getTableMetaList();
        //获取GoverMetrc信息
        List<GovernanceMetric> governanceMetricList=governanceMetricService.list(new QueryWrapper<GovernanceMetric>().eq("is_disabled","1"));
        //简历tableName的key-value结构，方面相似表判断使用
        HashMap<String,TableMetaInfo> tableMetaInfoHashMap=new HashMap<>();
        for (TableMetaInfo tableMetaInfo : tableMetaList) {
            tableMetaInfoHashMap.put(tableMetaInfo.getSchemaName()+"."+tableMetaInfo.getTableName(),tableMetaInfo);
        }
        //创建考评详情类的集合
        List<GovernanceAssessDetail> list=new ArrayList<>();
        //根据metric_code去application容器获取对应的子类
        for (TableMetaInfo tableMetaInfo : tableMetaList) {
            for (GovernanceMetric governanceMetric : governanceMetricList) {
                //将需要传入的参数封装在一个类中，实现解耦，动态添加
                InputDetail inputDetail=new InputDetail();
                inputDetail.setTableMetaInfo(tableMetaInfo);
                inputDetail.setGovernanceMetric(governanceMetric);
                inputDetail.setAccessDate(accessDate);
                inputDetail.setTableMetaInfoHashMap(tableMetaInfoHashMap);
                //从容器中获取Access父类，并指向对应的子类方法
                Access bean = springApplication.getBean(governanceMetric.getMetricCode(), Access.class);
                //调用对应的实现类，得到考评详情
                GovernanceAssessDetail governanceAssessDetail = bean.mainAccess(inputDetail);
                list.add(governanceAssessDetail);
            }
        }
        //保存导数据库
        saveOrUpdateBatch(list);
    }
}
