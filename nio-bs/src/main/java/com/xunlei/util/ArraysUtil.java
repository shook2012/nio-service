package com.xunlei.util;

import java.util.Arrays;

/**
 * ��� Arrays�д󲿷ַ������ܴ��� Object�����,Ҳ���� ���÷���ǰ��������δ֪�����
 * 
 * @author ZengDong
 * @since 2011-5-11 ����01:52:09
 */
public class ArraysUtil {

    /**
     * �ж����������������ͣ��Ƿ����
     * 
     * @param e1
     * @param e2
     * @return
     */
    public static boolean equals(Object e1, Object e2) {
        boolean eq = false;
        if (e1 instanceof Object[] && e2 instanceof Object[]) {
            eq = Arrays.equals((Object[]) e1, (Object[]) e2);
        } else if (e1 instanceof byte[] && e2 instanceof byte[]) {
            eq = Arrays.equals((byte[]) e1, (byte[]) e2);
        } else if (e1 instanceof short[] && e2 instanceof short[]) {
            eq = Arrays.equals((short[]) e1, (short[]) e2);
        } else if (e1 instanceof int[] && e2 instanceof int[]) {
            eq = Arrays.equals((int[]) e1, (int[]) e2);
        } else if (e1 instanceof long[] && e2 instanceof long[]) {
            eq = Arrays.equals((long[]) e1, (long[]) e2);
        } else if (e1 instanceof char[] && e2 instanceof char[]) {
            eq = Arrays.equals((char[]) e1, (char[]) e2);
        } else if (e1 instanceof float[] && e2 instanceof float[]) {
            eq = Arrays.equals((float[]) e1, (float[]) e2);
        } else if (e1 instanceof double[] && e2 instanceof double[]) {
            eq = Arrays.equals((double[]) e1, (double[]) e2);
        } else if (e1 instanceof boolean[] && e2 instanceof boolean[]) {
            eq = Arrays.equals((boolean[]) e1, (boolean[]) e2);
        } else if (null == e1 && null == e2) { // ˫����null������true
            eq = true;
        } else if (null != e1) {
            eq = e1.equals(e2);
        }
        return eq;
    }

    /**
     * �ж����������������ͣ��Ƿ�������
     * 
     * @param e1
     * @param e2
     * @return
     */
    public static boolean deepEquals(Object e1, Object e2) {
        boolean eq = false;
        if (e1 instanceof Object[] && e2 instanceof Object[]) {
            eq = Arrays.deepEquals((Object[]) e1, (Object[]) e2);
        } else if (e1 instanceof byte[] && e2 instanceof byte[]) {
            eq = Arrays.equals((byte[]) e1, (byte[]) e2);
        } else if (e1 instanceof short[] && e2 instanceof short[]) {
            eq = Arrays.equals((short[]) e1, (short[]) e2);
        } else if (e1 instanceof int[] && e2 instanceof int[]) {
            eq = Arrays.equals((int[]) e1, (int[]) e2);
        } else if (e1 instanceof long[] && e2 instanceof long[]) {
            eq = Arrays.equals((long[]) e1, (long[]) e2);
        } else if (e1 instanceof char[] && e2 instanceof char[]) {
            eq = Arrays.equals((char[]) e1, (char[]) e2);
        } else if (e1 instanceof float[] && e2 instanceof float[]) {
            eq = Arrays.equals((float[]) e1, (float[]) e2);
        } else if (e1 instanceof double[] && e2 instanceof double[]) {
            eq = Arrays.equals((double[]) e1, (double[]) e2);
        } else if (e1 instanceof boolean[] && e2 instanceof boolean[]) {
            eq = Arrays.equals((boolean[]) e1, (boolean[]) e2);
        } else if (null == e1 && null == e2) { // ˫����null������true
            eq = true;
        } else if (null != e1) {
            eq = e1.equals(e2);
        }
        return eq;
    }

    /**
     * ���������͵Ķ�������ת��Ϊ�ַ���
     * 
     * <pre>
     * jdk�е�Array.deepToString�������ܴ���δ֪���͵Ķ���,ֻ�ܴ��� Object[],��������Щ��ǿ
     * �Դ������½������Ķ��������
     * </pre>
     * 
     * @param array
     * @return
     */
    public static String deepToString(Object array) {
        if (array == null) {
            return "null";
        }
        Class<?> clazz = array.getClass();
        if (clazz.isArray()) {
            if (clazz == byte[].class) {
                return Arrays.toString((byte[]) array);
            } else if (clazz == short[].class) {
                return Arrays.toString((short[]) array);
            } else if (clazz == int[].class) {
                return Arrays.toString((int[]) array);
            } else if (clazz == long[].class) {
                return Arrays.toString((long[]) array);
            } else if (clazz == char[].class) {
                return Arrays.toString((char[]) array);
            } else if (clazz == float[].class) {
                return Arrays.toString((float[]) array);
            } else if (clazz == double[].class) {
                return Arrays.toString((double[]) array);
            } else if (clazz == boolean[].class) {
                return Arrays.toString((boolean[]) array);
            } else { // array is an array of object references
                return Arrays.deepToString((Object[]) array);
            }
        }
        return array.toString();
    }

    /**
     * ���������͵Ķ���ת��Ϊ�ַ���
     * 
     * <pre>
     * jdk�е�Array.toString�������ܴ���δ֪���͵Ķ���,ֻ����֪�������͵����,��������Щ��ǿ
     * �Դ������½������Ķ��������
     * </pre>
     * 
     * @param array
     * @return
     */
    public static String toString(Object array) {
        if (array == null) {
            return "null";
        }
        Class<?> clazz = array.getClass();
        if (clazz.isArray()) {
            if (clazz == byte[].class) {
                return Arrays.toString((byte[]) array);
            } else if (clazz == short[].class) {
                return Arrays.toString((short[]) array);
            } else if (clazz == int[].class) {
                return Arrays.toString((int[]) array);
            } else if (clazz == long[].class) {
                return Arrays.toString((long[]) array);
            } else if (clazz == char[].class) {
                return Arrays.toString((char[]) array);
            } else if (clazz == float[].class) {
                return Arrays.toString((float[]) array);
            } else if (clazz == double[].class) {
                return Arrays.toString((double[]) array);
            } else if (clazz == boolean[].class) {
                return Arrays.toString((boolean[]) array);
            } else { // array is an array of object references
                return Arrays.toString((Object[]) array);
            }
        }
        return array.toString();
    }
}
