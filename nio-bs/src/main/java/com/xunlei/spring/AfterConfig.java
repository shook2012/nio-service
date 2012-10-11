package com.xunlei.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * 原来的 AfterConfig方法内部 是不能调用 BeanUtil.getTypedBean方法的，会报 Bootstarp.CONTEXT 是空（因为 程序启动顺序问题）
 * 
 * 2012-05-28 改成 AfterConfig可以调用BeanUtil.getTypedBean方法，并且增加一个Annotation
 *   AfterBoostrap 表示 在程序启动时，在Spring整体BeanContext完全实例化完后，再来遍历调用一次
 * 而AfterConfig   表示 在程序启动时，以及在运行时动态改变配置时，调用
 * 
 * 也就是@AfterBoostrap  不会在动态改变配置时调用，而@AfterConfig调用情况更多点
 * 
 * 所以注意使用的场景
 * 
 * @author ZengDong
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterConfig {
}
