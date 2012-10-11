package com.xunlei.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * <pre>
 * �ж�һЩ��������(Collectin,Map,String,array)�Ƿ�Ϊ�ջ����ڲ�
 * 1.null����true
 * 2.size/length==0ʱ����true
 * </pre>
 * 
 * @author ZengDong
 * @since 2010-11-10 ����09:37:14
 */
public class EmptyChecker {

    /**
     * boolean���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isEmpty(boolean[] array) {
        return array == null || array.length == 0;
    }

    /**
     * byte���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isEmpty(byte[] array) {
        return array == null || array.length == 0;
    }

    /**
     * char���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isEmpty(char[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Collection���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    /**
     * double���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isEmpty(double[] array) {
        return array == null || array.length == 0;
    }

    /**
     * float���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isEmpty(float[] array) {
        return array == null || array.length == 0;
    }

    /**
     * int���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    /**
     * map���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isEmpty(Map<?, ?> m) {
        return m == null || m.isEmpty();
    }

    /**
     * Object���͵Ķ����Ƿ�Ϊ��
     * 
     * <pre>
     * ���� commons�е� CollectionUtils.sizeIsEmpty
     * ��֮ͬ������ objΪ��ʱ,����true
     * @param object
     * @return Ϊ�շ���true���򷵻�false
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
     * Object���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * short���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isEmpty(short[] array) {
        return array == null || array.length == 0;
    }

    /**
     * ͬ{@link StringTools}.isEmpty()���� String�����Ƿ�Ϊ��
     * 
     * @param str
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * StringBuilder�Ķ����Ƿ�Ϊ��
     * 
     * @param sb
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isEmpty(StringBuilder sb) {
        return sb == null || sb.length() == 0;
    }

    /**
     * boolean���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isNotEmpty(boolean[] array) {
        return !isEmpty(array);
    }

    /**
     * byte���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isNotEmpty(byte[] array) {
        return !isEmpty(array);
    }

    /**
     * char���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isNotEmpty(char[] array) {
        return !isEmpty(array);
    }

    /**
     * Collection���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isNotEmpty(Collection<?> c) {
        return !isEmpty(c);
    }

    /**
     * double���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isNotEmpty(double[] array) {
        return !isEmpty(array);
    }

    /**
     * float���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isNotEmpty(float[] array) {
        return !isEmpty(array);
    }

    /**
     * int���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isNotEmpty(int[] array) {
        return !isEmpty(array);
    }

    /**
     * Map���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isNotEmpty(Map<?, ?> m) {
        return !isEmpty(m);
    }

    /**
     * Object���͵Ķ����Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }

    /**
     * Object���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    /**
     * short���͵������Ƿ�Ϊ��
     * 
     * @param array
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isNotEmpty(short[] array) {
        return !isEmpty(array);
    }

    /**
     * ͬ{@link StringTools}.isEmpty()���� String�����Ƿ�Ϊ��
     * 
     * @param str
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * StringBuilder�Ķ����Ƿ�Ϊ��
     * 
     * @param sb
     * @return Ϊ�շ���true���򷵻�false
     */
    public static boolean isNotEmpty(StringBuilder sb) {
        return !isEmpty(sb);
    }

    /**
     * Ĭ�ϵĹ��췽��
     */
    private EmptyChecker() {
    }
}
