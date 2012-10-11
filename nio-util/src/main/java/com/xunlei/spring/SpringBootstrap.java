package com.xunlei.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author ����
 * @since 2012-5-25 ����7:45:10
 */
public class SpringBootstrap {

    private static ApplicationContext CONTEXT;

    public static ApplicationContext load(String... springConfigLocations) {
        if (CONTEXT != null) {
            throw new IllegalAccessError("SpringBootstrap.CONTEXT is setted,you should start app by SpringBootstrap.main() only once");
        }
        ApplicationContext context = CONTEXT = new ClassPathXmlApplicationContext(springConfigLocations);
//        Bootstrap.CONTEXT = context; // Ϊ�˼����ϵ�CONTEXT
        ConfigAnnotationBeanPostProcessor pp = BeanUtil.getTypedBean(context, "configAnnotationBeanPostProcessor");
        pp.postProcessAfterBootstrap(context);// ����ʹ��Bootstrap����AfterConfig
        return context;
    }

    public static ApplicationContext getContext() {
        if (CONTEXT == null) {
            throw new NullPointerException("SpringBootstrap.CONTEXT is null,solution:you should start app by SpringBootstrap.load()");
        }
        return CONTEXT;
    }

    private SpringBootstrap() {
    }
}
