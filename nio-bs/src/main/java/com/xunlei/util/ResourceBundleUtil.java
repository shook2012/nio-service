package com.xunlei.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.slf4j.Logger;

/**
 * 资源包绑定工具类
 * 
 * @author ZengDong
 * @since 2010-6-7 下午11:35:30
 */
public class ResourceBundleUtil {

    /**
     * 资源包实例默认是缓存的，为了防止资源包缓存，编写ResourceBundle.Control的子类，重写getTimeToLive方法
     * 
     * @author ZengDong
     */
    private static class NoCacheResourceBundleControl extends ResourceBundle.Control {

        /**
         * 设置资源包为不缓存
         */
        @Override
        public long getTimeToLive(String baseName, Locale locale) {
            return ResourceBundle.Control.TTL_DONT_CACHE;
        }
    }

    private static final Logger log = Log.getLogger();
    private static final NoCacheResourceBundleControl noCacheResourceBundleControl = new NoCacheResourceBundleControl();

    /**
     * 重新加载资源包
     * 
     * @param filterName 资源包路径
     * @return
     */
    public static ResourceBundle reload(String filterName) {
        try {
            return ResourceBundle.getBundle(filterName, Locale.ENGLISH, noCacheResourceBundleControl);
        } catch (MissingResourceException e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
