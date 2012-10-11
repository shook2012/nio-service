package com.xunlei.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ZengDong
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Config {

    /**
     * ��Properties�ļ��ж�Ӧ��KEY����
     */
    String value() default "";

    /**
     * �Ƿ�����ͨ����������ֵ
     */
    boolean resetable() default false;

    /**
     * Ԫ������֮��ķָ���
     */
    String split() default ",";

    /**
     * Key-Value֮��ķָ���
     */
    String splitKeyValue() default ":";
}
