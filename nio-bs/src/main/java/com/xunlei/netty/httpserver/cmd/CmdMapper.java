package com.xunlei.netty.httpserver.cmd;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * ��һ��CMD������URL���ñ���
 * ע��:
 * CmdPath  ������� �ֲ����·��
 * CmdMapper������� ����·��
 * 
 * ����CmdOverride��ʹ�ã����԰�ԭ��Ĭ�ϵ����ָ��ǵ�
 */
@Target({ java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CmdMapper {

    public abstract String[] value();

}
