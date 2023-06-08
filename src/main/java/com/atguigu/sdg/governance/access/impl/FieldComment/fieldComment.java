package com.atguigu.sdg.governance.access.impl.FieldComment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.sdg.governance.access.Access;
import com.atguigu.sdg.governance.bean.GovernanceAssessDetail;
import com.atguigu.sdg.governance.bean.InputDetail;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 规范性--是否有确实字段备注
 */
@Component("FIELD_COMMENT")
public class fieldComment extends Access {


    @Override
    protected void access(InputDetail inputDetail, GovernanceAssessDetail governanceAssessDetail) throws ParseException {
        //获取表字段信息
        String colNameJson = inputDetail.getTableMetaInfo().getColNameJson();
        List<JSONObject> jsonObjects = JSON.parseArray(colNameJson, JSONObject.class);
        Integer sum=0;
        Set<String> set=new HashSet<>();
        //循环遍历，获取没有备注的字段
        for (JSONObject jsonObject : jsonObjects) {
            String comment = jsonObject.getString("comment");
            if(comment==null||comment.equals("")){
                sum++;
                //将确实备注的字段添加
                set.add(jsonObject.getString("name"));
            }
        }
        if (sum==0){
            //表示没有缺失任何字段备注，直接返回
            return;
        }
        if(sum==jsonObjects.size()){
            //全部缺失，给0分
            governanceAssessDetail.setAssessScore(BigDecimal.ZERO);
            governanceAssessDetail.setAssessProblem("缺失字段备注"+set);
        }else {
            //部分缺失
            int num=(jsonObjects.size()-sum)/jsonObjects.size()*10;
            governanceAssessDetail.setAssessScore(BigDecimal.valueOf(num));
            governanceAssessDetail.setAssessProblem("缺失部分字段备注"+set);
        }

    }
}
