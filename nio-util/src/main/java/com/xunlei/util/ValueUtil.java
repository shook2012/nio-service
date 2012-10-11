package com.xunlei.util;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ��Ҫ����������͵�ת�����ܹ���ɵ��������ת���������ж��������ת��
 * 
 * @author ZengDong
 * @since 2010-6-3 ����04:33:18
 */
public class ValueUtil {

    public static final String[] REF_ARRAY_STRING = new String[0];
    public static final Boolean[] REF_ARRAY_BOOLEAN = new Boolean[0];
    public static final Byte[] REF_ARRAY_BYTE = new Byte[0];
    public static final Character[] REF_ARRAY_CHARACTER = new Character[0];
    public static final Short[] REF_ARRAY_SHORT = new Short[0];
    public static final Integer[] REF_ARRAY_INTEGER = new Integer[0];
    public static final Long[] REF_ARRAY_LONG = new Long[0];
    public static final Float[] REF_ARRAY_FLOAT = new Float[0];
    public static final Double[] REF_ARRAY_DOUBLE = new Double[0];
    private static final IllegalArgumentException classNotBasicTypeException = new IllegalArgumentException("classNotBasicType");

    /**
     * �����͵�ת�ͷ����б�
     */
    private static final Map<Type, Method> valueOfMethod = initValueOfMethod();

    /**
     * �ַ�����ת����boolean��
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static boolean getBoolean(String value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value.equals("true") || value.equalsIgnoreCase("y") || value.equals("1")) {
            return true;
        }
        return false;
    }

    /**
     * �ַ�����ת����byte��
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static byte getByte(String value, byte defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Byte.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * �ַ���ת����Character�ͣ�ֱ��ȡ��һ���ַ�
     * 
     * @param str
     * @return
     */
    public static Character getCharacter(String str) {
        return str.charAt(0);
    }

    /**
     * �ַ���ת����char��
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static char getCharacter(String value, char defaultValue) {
        if (StringTools.isEmpty(value)) {
            return defaultValue;
        }
        return value.charAt(0);
    }

    /**
     * �ַ���ת����char��
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static double getDouble(String value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * String ת���� float��
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static float getFloat(String value, float defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * String ��ת����Integer ��
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static int getInteger(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * String��ת���� Long��
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static long getLong(String value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * String �� ת���� Short��
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static short getShort(String value, short defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Short.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * �������valueΪnull�ͷ���defaultValue����ͷ���value
     */
    public static String getString(String value, String defaultValue) {
        if (null == value) {
            return defaultValue;
        }
        return value;
    }

    /**
     * ��ȡ��String����ת���������͵ķ���
     * 
     * @param clazz
     * @return
     */
    protected static Method getValueOfMethod(Class<?> clazz) {
        Method m = valueOfMethod.get(clazz);
        if (m == null) {
            throw classNotBasicTypeException;
        }
        return m;
    }

    /**
     * ��û�������-��������ת��String���͵ķ����� ӳ���б�
     * 
     * @return
     */
    private static Map<Type, Method> initValueOfMethod() {
        Map<Type, Method> valueOfMethods = new HashMap<Type, Method>();
        try {
            valueOfMethods.put(int.class, Integer.class.getMethod("valueOf", String.class));
            valueOfMethods.put(Integer.class, Integer.class.getMethod("valueOf", String.class));
            valueOfMethods.put(long.class, Long.class.getMethod("valueOf", String.class));
            valueOfMethods.put(Long.class, Long.class.getMethod("valueOf", String.class));
            valueOfMethods.put(float.class, Float.class.getMethod("valueOf", String.class));
            valueOfMethods.put(Float.class, Float.class.getMethod("valueOf", String.class));
            valueOfMethods.put(double.class, Double.class.getMethod("valueOf", String.class));
            valueOfMethods.put(Double.class, Double.class.getMethod("valueOf", String.class));
            valueOfMethods.put(short.class, Short.class.getMethod("valueOf", String.class));
            valueOfMethods.put(Short.class, Short.class.getMethod("valueOf", String.class));
            valueOfMethods.put(boolean.class, Boolean.class.getMethod("valueOf", String.class));
            valueOfMethods.put(Boolean.class, Boolean.class.getMethod("valueOf", String.class));
            valueOfMethods.put(byte.class, Byte.class.getMethod("valueOf", String.class));
            valueOfMethods.put(Byte.class, Byte.class.getMethod("valueOf", String.class));
            valueOfMethods.put(String.class, String.class.getMethod("valueOf", Object.class));
            // ����Character.valueOf�����Ĳ���Ϊchar���Դ���Ҫʹ��һ������ķ���
            valueOfMethods.put(char.class, ValueUtil.class.getMethod("getCharacter", String.class));
            valueOfMethods.put(Character.class, ValueUtil.class.getMethod("getCharacter", String.class));
        } catch (Exception e) {
        }
        return valueOfMethods;
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add("3");
        System.out.println(valueOf(list, int.class));
        System.out.println(valueOf(list, short.class));
        System.out.println(Arrays.toString(valueOfToArray(list, REF_ARRAY_INTEGER)));
    }

    /**
     * <pre>
     * ����Collection�д�ŵĶ����ŵ� Array��
     * �˷������Դ��� ԭʼ��������,�ʷ���ֵ�� Object,��õ�ֵ����ת��
     * @param <T>
     * @param coll  Collection����
     * @param clazz  Collection�ж��������
     * @return
     */
    public static <T> Object toArray(Collection<T> coll, Class<T> clazz) {
        Object result = Array.newInstance(clazz, coll.size());
        int i = 0;
        for (T obj : coll) {
            Array.set(result, i++, obj);
        }
        return result;
    }

    /**
     * <pre>
     * Collection -> Array
     * ����Collection�д�ŵĶ����ŵ� Array��
     * �˷���ʹ������refArray�ķ�ʽ,���ܴ��� ԭʼ��������,������ֵ֧�ַ���
     * @param <T>
     * @param coll
     * @param refArray
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Collection<T> coll, T[] refArray) {
        Object result = Array.newInstance(refArray.getClass().getComponentType(), coll.size());
        int i = 0;
        for (T obj : coll) {
            Array.set(result, i++, obj);
        }
        return (T[]) result;
    }

    /**
     * <pre>
     * Collection<String> -> Collection<T> (ԭ��������)
     * ��������String���͵�����ת����Ŀ�����ͣ���������µ�������
     * ��������ַ���Collectionת��Ϊָ�����͵�Collection��ֻ֧�ֻ����ͣ�
     * @param <T>
     * @param strList  ԭ����������
     * @param componentClazz ���ؽ���д�ŵ���������
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> valueOf(Collection<String> strList, Class<T> componentClazz) {
        Method method = getValueOfMethod(componentClazz);
        if (strList == null || strList.size() == 0) {
            return Collections.emptyList();// TODO:��nullʱ,��ôȥ���������
        }
        try {
            Collection<T> result = strList.getClass().newInstance();
            for (String a : strList) {
                try {
                    result.add((T) method.invoke(null, a));
                } catch (Exception e) {// TODO:�Ƿ����־
                }
            }
            return result;
        } catch (Exception e1) {
            return Collections.emptyList();// TODO:��ôȥ���������
        }
    }

    /**
     * <pre>
     * Collection<String> -> Collection<T> (�¼�������)
     * ��������String���͵�����ת����Ŀ�����ͣ���������ĵ������У��µ����������ÿ�����ǰָ��
     * ��������ַ���Collectionת��Ϊָ�����͵ļ��ϣ�ֻ֧�ֻ����ͣ�
     * @param <T>
     * @param strList ԭ����������
     * @param componentClazz Ŀ������
     * @param result ������������
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> valueOf(Collection<String> strList, Class<T> componentClazz, Collection<T> result) {
        Method method = getValueOfMethod(componentClazz);
        if (strList != null) {
            for (String a : strList) {
                try {
                    result.add((T) method.invoke(null, a));
                } catch (Exception e) {// TODO:�Ƿ����־
                }
            }
        }
        return result;
    }

    /**
     * ��ָ������ת����Ŀ������
     * 
     * @param <T> ����
     * @param obj ��ת���Ķ���
     * @param destClazz Ŀ��������
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T valueOf(Object obj, Class<T> destClazz) throws Exception {
        Method method = getValueOfMethod(destClazz);
        return (T) method.invoke(null, obj.toString());
    }

    /**
     * ��ָ������ת��ΪĿ�����ͣ����ת��ʧ�ܷ���Ĭ�ϵ�����
     * 
     * @param <T>
     * @param obj ��ת���Ķ���
     * @param destClazz Ŀ������
     * @param defaultValue Ĭ��ֵ
     * @return
     */
    public static <T> T valueOf(Object obj, Class<T> destClazz, T defaultValue) {
        try {
            return valueOf(obj, destClazz);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * <pre>
     * Collection<String> 
     *   --valueOf()-->   List<T>
     *   --toArray()-->   T[]
     * ��������String���͵Ķ���ת��Ϊָ�����ͣ��������Array������
     * ��Ϊʹ��ָ��componentClazz,��֧��ԭʼ��������,������ֵ��Object,����ת��
     * </pre>
     * 
     * @param <T>
     * @param strList
     * @param componentClazz
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes", "cast" })
    public static <T> Object valueOfToArray(Collection<String> strList, Class<T> componentClazz) {
        return toArray((Collection<T>) valueOf(strList, componentClazz, new ArrayList(strList.size())), componentClazz);
    }

    /**
     * <pre>
     * Collection<String> 
     *   --valueOf()-->   List<T>
     *   --toArray()-->   T[]
     * ��������String���͵Ķ���ת���������Ͳ�����������
     * ��Ϊʹ��refArray,�ʲ�֧��ԭʼ��������
     * </pre>
     * 
     * @param <T>
     * @param strList ԭ����������
     * @param refArray ���������������
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes", "cast" })
    public static <T> T[] valueOfToArray(Collection<String> strList, T[] refArray) {
        return toArray((Collection<T>) valueOf(strList, refArray.getClass().getComponentType(), new ArrayList(strList.size())), refArray);
    }

    /**
     * Ĭ�Ϲ��췽��
     */
    private ValueUtil() {
    }
}
