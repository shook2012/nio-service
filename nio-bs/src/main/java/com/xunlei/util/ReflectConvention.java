package com.xunlei.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.beans.BeanUtils;

/**
 * �����漰����һЩ���÷���
 * 
 * <pre>
 *  org.springframework.beans.BeanUtils
 *  Introspector.getBeanInfo 
 *  org.springframework.util.ReflectionUtils
 *  
 *  TODO:��ô��spring,introspector����
 * 
 * @author ����
 * @since 2009-3-10 ����11:38:05
 */
public class ReflectConvention {

    /**
     * �Ƿ�ӵ��Spring��֧��
     */
    public static final boolean SPRING_ENABLE;

    static {
        SPRING_ENABLE = isClassFound("org.springframework.beans.BeanUtils");
        if (!SPRING_ENABLE) {
            System.err.println("ReflectConvention.SPRING_ENABLE=false");
        }
    }

    /**
     * ���������Ե�getter����
     * 
     * @param clazz ��
     * @param field ����
     * @return
     * @throws SecurityException ��ȫ�쳣
     * @throws NoSuchMethodException û�д˷����쳣
     */
    public static Method buildGetterMethod(Class<?> clazz, Field field) throws SecurityException, NoSuchMethodException {
        if (SPRING_ENABLE) {
            String fieldName = field.getName();
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(clazz, fieldName);
            if (pd == null) {
                throw new NoSuchMethodException(clazz.getName() + "." + fieldName);
            }
            return pd.getReadMethod();
        }
        String prefix = "get";
        if (field.getType().equals(boolean.class)) {
            prefix = "is";
        }
        String getterStr = prefix + capitalize(field.getName());
        return clazz.getDeclaredMethod(getterStr);
    }

    /**
     * ���������Ե�setter����
     * 
     * @param clazz ��
     * @param field ����
     * @param parameterType ���Ե�����
     * @return
     * @throws SecurityException ��ȫ�쳣
     * @throws NoSuchMethodException û�д˷����쳣
     */
    public static Method buildSetterMethod(Class<?> clazz, Field field, Class<?> parameterType) throws SecurityException, NoSuchMethodException {
        if (SPRING_ENABLE) {
            String fieldName = field.getName();
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(clazz, fieldName);
            if (pd == null) {
                throw new NoSuchMethodException(clazz.getName() + "." + fieldName);
            }
            return pd.getWriteMethod();
        }
        String getterStr = "set" + capitalize(field.getName());
        return clazz.getDeclaredMethod(getterStr, parameterType);
    }

    /**
     * ���ַ���������ĸ��ɴ�д
     * 
     * @param name
     * @return
     */
    private static String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * ���õݹ���һ�����ָ�����ԣ�����Ҳ�����ȥ����������ֱ�����ϲ�Object����Ϊֹ��
     * 
     * @param clazz ��
     * @param fieldName �����Ǹ�
     * @return
     * @throws Exception
     */
    public static Field getDeclaredField(Class<?> clazz, String fieldName) throws Exception {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            try {
                field = clazz.getField(fieldName);
            } catch (NoSuchFieldException ex) {
                if (clazz.getSuperclass() == null) {
                    throw ex;
                }
                field = getDeclaredField(clazz.getSuperclass(), fieldName);
            }
        }
        return field;
    }

    /**
     * ���õݹ���һ�����ָ������������Ҳ�����ȥ����������ֱ�����ϲ�Object����Ϊֹ��
     * 
     * @param clazz ��
     * @param methodName ��������
     * @param classes �������β�����
     * @return
     * @throws Exception
     */
    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... classes) throws Exception {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(methodName, classes);
            } catch (NoSuchMethodException ex) {
                if (clazz.getSuperclass() == null) {
                    throw ex;
                }
                method = getDeclaredMethod(clazz.getSuperclass(), methodName, classes);
            }
        }
        return method;
    }

    /**
     * ���data����field���Ե�ֵ
     * 
     * @param data ����
     * @param field ����
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Object getValue(Object data, Field field) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = buildGetterMethod(field.getDeclaringClass(), field);
        return method.invoke(data);
    }

    /**
     * ����ָ�����࣬�������Ƿ���سɹ�
     * 
     * @param clazzName ���ȫ��
     * @return
     */
    public static boolean isClassFound(String clazzName) {
        try {
            Class.forName(clazzName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * ����ָ�������ɸ��࣬�������Ƿ���سɹ�
     * 
     * @param clazzNames ��ȫ���б�
     * @return
     */
    public static boolean isClassFound(String... clazzNames) {
        try {
            for (String clazzName : clazzNames) {
                Class.forName(clazzName);
            }
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * ������data������field����Ϊֵvalue
     * 
     * @param data ����
     * @param field ����
     * @param value ֵ
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void setValue(Object data, Field field, Object value) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = field.getDeclaringClass();
        Method method = buildSetterMethod(clazz, field, field.getType());
        method.invoke(data, value);
    }

    /**
     * Ĭ�ϵĹ��췽��
     */
    private ReflectConvention() {
    }
}
