package com.xunlei.util;

import java.util.regex.Pattern;

/**
 * ������֤ר�ù�����
 * 
 * @since 2010-11-22
 * @author hujiachao
 */
public class ValidateUtil {

    private static Pattern EMAIL = Pattern.compile("\\w+([-.]\\w+)*@\\w+([-]\\w+)*\\.(\\w+([-]\\w+)*\\.)*[a-z,A-Z]{2,3}");

    /**
     * �ж��Ƿ��ǺϷ���email��ַ
     */
    public static boolean isValidEmail(String email) {
        if (StringTools.isNotEmpty(email)) {
            return EMAIL.matcher(email).matches();
        }
        return false;
    }

    /**
     * �ж��Ƿ��ǺϷ���email��ַ
     */
    public static boolean isNotValidEmail(String email) {
        return !isValidEmail(email);
    }
}
