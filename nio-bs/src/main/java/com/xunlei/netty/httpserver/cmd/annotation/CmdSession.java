package com.xunlei.netty.httpserver.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.xunlei.netty.httpserver.util.CmdSessionType;

/**
 * cmd��¼̬Ҫ��
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CmdSession {

    /** ��¼̬Ҫ������ */
    CmdSessionType type();

    /** �Ե�¼̬���������������Էŵ����� */
    String desc() default "";
}
