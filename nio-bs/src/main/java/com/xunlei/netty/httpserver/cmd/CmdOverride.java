package com.xunlei.netty.httpserver.cmd;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ����CmdPath��CmdMapper�����ã����ǵ�ԭ����Ĭ����
 * 
 * @author ZengDong
 * @since 2012-3-28 ����11:33:00
 */
@Target({ java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CmdOverride {
}
