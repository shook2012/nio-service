package com.xunlei.netty.httpserver.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * �ӿڷ�������������CMD����
 * 
 * @since 2012-3-1
 * @author hujiachao
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CmdDescr {

    /**
     * �ӿڷ�������
     */
    String value();
}
