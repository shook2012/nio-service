package com.xunlei.logback.property;

import ch.qos.logback.core.PropertyDefinerBase;
import com.xunlei.util.HttpUtil;

/**
 * 本地IP类
 * 
 * @author ZengDong
 * @since 2011-3-16 下午01:50:54
 */
public class LocalSampleIp extends PropertyDefinerBase {

    /**
     * 获得属性值
     */
    @Override
    public String getPropertyValue() {
        return HttpUtil.getLocalSampleIP();
    }
}
