package com.xunlei.netty.httpserver.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * 给接口定类别
 * 可以在cmd的type上，也可以在具体一个cmd方法上来定义
 * 可以定义多个类别
 * 
 * 类似于标签
 * 
 * 如果没有此标识，默认是以一个cmd自己就成一个类别
 * 
 * @author ZengDong
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface CmdCategory {

    String[] value();
}
