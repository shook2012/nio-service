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
     * 在Properties文件中对应的KEY名称
     */
    String value() default "";

    /**
     * 是否允许通过命令重设值
     */
    boolean resetable() default false;

    /**
     * 元素属性之间的分隔符
     */
    String split() default ",";

    /**
     * Key-Value之间的分隔符
     */
    String splitKeyValue() default ":";
}
