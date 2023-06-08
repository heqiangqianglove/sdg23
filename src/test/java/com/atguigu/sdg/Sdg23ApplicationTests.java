package com.atguigu.sdg;

import com.atguigu.sdg.governance.service.GovernanceAssessDetailService;
import com.atguigu.sdg.meta.service.TableMetaInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Sdg23ApplicationTests {
    @Autowired
    TableMetaInfoService tableMetaInfoService;
    @Autowired
    GovernanceAssessDetailService governanceAssessDetailService;
    @Test
    void contextLoads() {
        tableMetaInfoService.initMetaInfo("2023-06-05","gmall");
    }
    @Test
    void test2(){
        tableMetaInfoService.getTableMetaList();
    }

    @Test
    void test3(){
        governanceAssessDetailService.setGovernanceDetail("2023-06-06");
    }
}
