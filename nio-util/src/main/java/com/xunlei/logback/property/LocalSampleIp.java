package com.xunlei.logback.property;

import ch.qos.logback.core.PropertyDefinerBase;
import com.xunlei.util.HttpUtil;

/**
 * ����IP��
 * 
 * @author ZengDong
 * @since 2011-3-16 ����01:50:54
 */
public class LocalSampleIp extends PropertyDefinerBase {

    /**
     * �������ֵ
     */
    @Override
    public String getPropertyValue() {
        return HttpUtil.getLocalSampleIP();
    }
}
