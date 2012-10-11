package com.xunlei.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * <pre>
 * 判断一些容器对象(Collectin,Map,String,array)是否为空或无内部
 * 1.null返回true
 * 2.size/length==0时返回true
 * </pre>
 * 
 * @author ZengDong
 * @since 2010-11-10 下午09:37:14
 */
public class EmptyChecker {

    /**
     * boolean类型的数组是否为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isEmpty(boolean[] array) {
        return array == null || array.length == 0;
    }

    /**
     * byte类型的数组是否为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isEmpty(byte[] array) {
        return array == null || array.length == 0;
    }

    /**
     * char类型的数组是否为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isEmpty(char[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Collection类型的容器是否为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    /**
     * double类型的数组是否为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isEmpty(double[] array) {
        return array == null || array.length == 0;
    }

    /**
     * float类型的数组是否为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isEmpty(float[] array) {
        return array == null || array.length == 0;
    }

    /**
     * int类型的数组是否为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    /**
     * map类型的容器是否为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isEmpty(Map<?, ?> m) {
        return m == null || m.isEmpty();
    }

    /**
     * Object类型的对象是否为空
     * 
     * <pre>
     * 抄自 commons中的 CollectionUtils.sizeIsEmpty
     * 不同之外在于 obj为空时,返回true
     * @param object
     * @return 为空返回true否则返回false
     */
    public static boolean isEmpty(Object object) {
        if (object == null) {
            return true;
        }
        if (object instanceof Collection) {
            return ((Collection<?>) object).isEmpty();
        }
        if (object instanceof Map) {
            return ((Map<?, ?>) object).isEmpty();
        }
        if (object instanceof Object[]) {
            return (((Object[]) object).length == 0);
        }
        if (object instanceof Iterator<?>) {
            return (!(((Iterator<?>) object).hasNext()));
        }
        if (object instanceof Enumeration) {
            return (!(((Enumeration<?>) object).hasMoreElements()));
        }
        try {
            return (Array.getLength(object) == 0);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
        }
    }

    /**
     * Object类型的数组是否为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * short类型的数组是否为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isEmpty(short[] array) {
        return array == null || array.length == 0;
    }

    /**
     * 同{@link StringTools}.isEmpty()方法 String类型是否为空
     * 
     * @param str
     * @return 为空返回true否则返回false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * StringBuilder的对象是否为空
     * 
     * @param sb
     * @return 为空返回true否则返回false
     */
    public static boolean isEmpty(StringBuilder sb) {
        return sb == null || sb.length() == 0;
    }

    /**
     * boolean类型的数组是否不为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isNotEmpty(boolean[] array) {
        return !isEmpty(array);
    }

    /**
     * byte类型的数组是否不为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isNotEmpty(byte[] array) {
        return !isEmpty(array);
    }

    /**
     * char类型的数组是否不为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isNotEmpty(char[] array) {
        return !isEmpty(array);
    }

    /**
     * Collection类型的容器是否不为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isNotEmpty(Collection<?> c) {
        return !isEmpty(c);
    }

    /**
     * double类型的数组是否不为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isNotEmpty(double[] array) {
        return !isEmpty(array);
    }

    /**
     * float类型的数组是否不为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isNotEmpty(float[] array) {
        return !isEmpty(array);
    }

    /**
     * int类型的数组是否不为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isNotEmpty(int[] array) {
        return !isEmpty(array);
    }

    /**
     * Map类型的容器是否不为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isNotEmpty(Map<?, ?> m) {
        return !isEmpty(m);
    }

    /**
     * Object类型的对象是否不为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }

    /**
     * Object类型的数组是否不为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    /**
     * short类型的数组是否不为空
     * 
     * @param array
     * @return 为空返回true否则返回false
     */
    public static boolean isNotEmpty(short[] array) {
        return !isEmpty(array);
    }

    /**
     * 同{@link StringTools}.isEmpty()方法 String类型是否不为空
     * 
     * @param str
     * @return 为空返回true否则返回false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * StringBuilder的对象是否不为空
     * 
     * @param sb
     * @return 为空返回true否则返回false
     */
    public static boolean isNotEmpty(StringBuilder sb) {
        return !isEmpty(sb);
    }

    /**
     * 默认的构造方法
     */
    private EmptyChecker() {
    }
}
