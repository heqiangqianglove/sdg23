package com.atguigu.sdg.governance.bean;

import com.atguigu.sdg.meta.bean.TableMetaInfo;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class InputDetail {
    private String accessDate;
    private GovernanceMetric governanceMetric;
    private TableMetaInfo tableMetaInfo;
    HashMap<String,TableMetaInfo> tableMetaInfoHashMap;
}
