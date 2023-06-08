package com.atguigu.sdg.governance.access.impl.TableSimilar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.sdg.governance.access.Access;
import com.atguigu.sdg.governance.bean.GovernanceAssessDetail;
import com.atguigu.sdg.governance.bean.InputDetail;
import com.atguigu.sdg.meta.bean.TableMetaInfo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 存储 -- 是否存在相似表
 */
@Component("TABLE_SIMILAR")
public class tableSimilar extends Access {

    @Override
    protected void access(InputDetail inputDetail, GovernanceAssessDetail governanceAssessDetail) throws ParseException {
        //获取当前表名
        TableMetaInfo tableMetaInfo = inputDetail.getTableMetaInfo();
        String tableName=tableMetaInfo.getSchemaName()+"."+tableMetaInfo.getTableName();
        //获取当前表的层级
        String dwLevel = tableMetaInfo.getTableMetaInfoExtra().getDwLevel();
        //获取当前表字段的JSON集合
        List<JSONObject> jsonObjects = JSON.parseArray(tableMetaInfo.getColNameJson(), JSONObject.class);
        //获取其它表的集合信息
        Set<Map.Entry<String, TableMetaInfo>> entries = inputDetail.getTableMetaInfoHashMap().entrySet();
        //记录相似表的表名
        HashSet<String> hashSet=new HashSet<>();
        //获取相似表阈值信息
        String metricParamsJson = inputDetail.getGovernanceMetric().getMetricParamsJson();
        JSONObject jsonObject1 = JSON.parseObject(metricParamsJson);
        BigDecimal percent = jsonObject1.getBigDecimal("percent");
        //遍历其它表的集合
        for (Map.Entry<String, TableMetaInfo> entry : entries) {
            //获取其它表的当前表名、层级、字段集合
            String otherTableName = entry.getKey();
            TableMetaInfo otherTableMetaInfo = entry.getValue();
            String otherDwLevel = otherTableMetaInfo.getTableMetaInfoExtra().getDwLevel();
            String colNameJson = otherTableMetaInfo.getColNameJson();
            List<JSONObject> otherJsonObjects = JSON.parseArray(colNameJson, JSONObject.class);

            //设置记录值  记录相似的表名，并去重
            Integer sum=0;
            //判断（1）ods层的表不进行相似比较 （2）排除与当前表相同的表名 （3）两个表处于相同的层级
            if(!dwLevel.equals("ODS")&&!tableName.equals(otherTableName)&&dwLevel.equals(otherDwLevel)){
                //循环对比字段
                for (JSONObject jsonObject : jsonObjects) {
                    for (JSONObject otherJsonObject : otherJsonObjects) {
                        String current=jsonObject.getString("name");
                        String other=otherJsonObject.getString("name");
                        if (current.equals(other))
                            sum++;
                    }
                }
            }
            //获取当前表的重复率
            BigDecimal bigDecimal = BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(jsonObjects.size()), 2, RoundingMode.HALF_UP).movePointRight(2);
            //跟阈值进行比较  当前表的重复率大于阈值 将相似表名添加进去
            if(bigDecimal.compareTo(percent)>0){
                hashSet.add(otherTableName);
            }
        }
        //判断hasSet是否有数据，有表示存在相似表
        if(hashSet.size()>0){
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("存在相似表"+hashSet);
        }
    }
}
