package com.xunlei.netty.httpserver.cmd.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * ���ӿڶ����
 * ������cmd��type�ϣ�Ҳ�����ھ���һ��cmd������������
 * ���Զ��������
 * 
 * �����ڱ�ǩ
 * 
 * ���û�д˱�ʶ��Ĭ������һ��cmd�Լ��ͳ�һ�����
 * 
 * @author ZengDong
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface CmdCategory {

    String[] value();
}
