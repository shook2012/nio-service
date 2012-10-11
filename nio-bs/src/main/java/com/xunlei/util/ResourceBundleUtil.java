package com.xunlei.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.slf4j.Logger;

/**
 * ��Դ���󶨹�����
 * 
 * @author ZengDong
 * @since 2010-6-7 ����11:35:30
 */
public class ResourceBundleUtil {

    /**
     * ��Դ��ʵ��Ĭ���ǻ���ģ�Ϊ�˷�ֹ��Դ�����棬��дResourceBundle.Control�����࣬��дgetTimeToLive����
     * 
     * @author ZengDong
     */
    private static class NoCacheResourceBundleControl extends ResourceBundle.Control {

        /**
         * ������Դ��Ϊ������
         */
        @Override
        public long getTimeToLive(String baseName, Locale locale) {
            return ResourceBundle.Control.TTL_DONT_CACHE;
        }
    }

    private static final Logger log = Log.getLogger();
    private static final NoCacheResourceBundleControl noCacheResourceBundleControl = new NoCacheResourceBundleControl();

    /**
     * ���¼�����Դ��
     * 
     * @param filterName ��Դ��·��
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
