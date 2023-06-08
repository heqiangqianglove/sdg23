package com.atguigu.sdg.governance.springApplication;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class springApplication implements ApplicationContextAware {
    private static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context=applicationContext;
    }

    /**
     * 从名称获取容器中的组件
     * @param name 子类@Component("TEC_OWNER")这个括号中的名字
     * @param aClass 在当前项目中 T指的是Assessor这个抽象类
     * @return
     * @param <T>
     */
    public  static <T> T getBean(String name,Class<T> aClass){
        return context.getBean(name,aClass);
    }
}
