package com.xunlei.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * ԭ���� AfterConfig�����ڲ� �ǲ��ܵ��� BeanUtil.getTypedBean�����ģ��ᱨ Bootstarp.CONTEXT �ǿգ���Ϊ ��������˳�����⣩
 * 
 * 2012-05-28 �ĳ� AfterConfig���Ե���BeanUtil.getTypedBean��������������һ��Annotation
 *   AfterBoostrap ��ʾ �ڳ�������ʱ����Spring����BeanContext��ȫʵ�������������������һ��
 * ��AfterConfig   ��ʾ �ڳ�������ʱ���Լ�������ʱ��̬�ı�����ʱ������
 * 
 * Ҳ����@AfterBoostrap  �����ڶ�̬�ı�����ʱ���ã���@AfterConfig������������
 * 
 * ����ע��ʹ�õĳ���
 * 
 * @author ZengDong
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterConfig {
}
