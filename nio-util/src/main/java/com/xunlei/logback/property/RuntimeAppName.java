package com.xunlei.logback.property;

import ch.qos.logback.core.PropertyDefinerBase;
import com.xunlei.util.SystemInfo;

/**
 * 运行时应用名称
 * 
 * @author ZengDong
 * @since 2011-3-16 下午01:50:54
 */
public class RuntimeAppName extends PropertyDefinerBase {

    /**
     * 获得属性值
     */
    @Override
    public String getPropertyValue() {
        return "[" + SystemInfo.LAUNCHER_NAME + "]";
        // String command = System.getProperty("sun.java.command");
        // return StringTools.isEmpty(command) ? ManagementFactory.getRuntimeMXBean().getName() : command.substring(command.lastIndexOf('.') + 1);
    }
}
