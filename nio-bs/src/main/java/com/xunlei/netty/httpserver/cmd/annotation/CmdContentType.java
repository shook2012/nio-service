package com.xunlei.netty.httpserver.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.xunlei.netty.httpserver.component.XLHttpResponse.ContentType;

/**
 * �ӿڷ��ظ�ʽ
 * 
 * @author ZengDong
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CmdContentType {

    ContentType[] value() default ContentType.json;

}
