package com.atguigu.sdg.meta.bean;

import lombok.Data;

@Data
public class TableReqInfo {
    /**
     * 库名
     */
    private String schemaName;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 数仓所在层级(ODSDWDDIMDWSADS) ( 来源: 附加)
     */
    private String dwLevel;
    /**
     * 每页总条数
     */
    private Integer pageSize;
    /**
     * 当前页
     */
    private Integer pageNo;
}
