package com.xunlei.util;

import java.util.regex.Pattern;

/**
 * 数据验证专用工具类
 * 
 * @since 2010-11-22
 * @author hujiachao
 */
public class ValidateUtil {

    private static Pattern EMAIL = Pattern.compile("\\w+([-.]\\w+)*@\\w+([-]\\w+)*\\.(\\w+([-]\\w+)*\\.)*[a-z,A-Z]{2,3}");

    /**
     * 判断是否是合法的email地址
     */
    public static boolean isValidEmail(String email) {
        if (StringTools.isNotEmpty(email)) {
            return EMAIL.matcher(email).matches();
        }
        return false;
    }

    /**
     * 判断是否不是合法的email地址
     */
    public static boolean isNotValidEmail(String email) {
        return !isValidEmail(email);
    }
}
