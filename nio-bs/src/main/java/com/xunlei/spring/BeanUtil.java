package com.xunlei.spring;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import com.xunlei.util.Log;

/**
 * ��Ҫ�����spring�����л��Bean���Լ���һ�����Ƿ�Ϊ�������͵��ж�
 * 
 * @author ZengDong
 * @since 2010-5-13 ����05:35:27
 */
public class BeanUtil {

    /**
     * ���������б�
     */
    private static final Set<Type> basicTypes = new HashSet<Type>(); // ���������б�

    private static final Logger log = Log.getLogger();

    static {
        basicTypes.add(int.class);
        basicTypes.add(Integer.class);
        basicTypes.add(long.class);
        basicTypes.add(Long.class);
        basicTypes.add(float.class);
        basicTypes.add(Float.class);
        basicTypes.add(double.class);
        basicTypes.add(Double.class);
        basicTypes.add(short.class);
        basicTypes.add(Short.class);
        basicTypes.add(boolean.class);
        basicTypes.add(Boolean.class);
        basicTypes.add(char.class);
        basicTypes.add(Character.class);
        basicTypes.add(byte.class);
        basicTypes.add(Byte.class);
        basicTypes.add(String.class);
    }

    /**
     * ��spring�����л��ĳ��Ķ���ʵ����ͨ������������ĸ���Сд��Ȼ��������ֵĶ���ʵ��
     * 
     * @param <T>
     * @param context spring������������
     * @param clazz ָ������
     * @return
     * @throws BeansException
     */
    @SuppressWarnings("unchecked")
    public static <T> T getTypedBean(ApplicationContext context, Class<T> clazz) throws BeansException {
        String oriName = clazz.getSimpleName();
        String name = oriName.substring(0, 1).toLowerCase() + oriName.substring(1);
        return (T) context.getBean(name);
    }

    /**
     * ��Spring�����л��ָ�����ֵĶ���ʵ��
     * 
     * @param <T>
     * @param context spring�����ĵ�����
     * @param name Ҫ��õ�bean������
     * @return
     * @throws BeansException
     */
    @SuppressWarnings("unchecked")
    public static <T> T getTypedBean(ApplicationContext context, String name) throws BeansException {
        return (T) context.getBean(name);
    }

    public static <T> T getTypedBean(Class<T> clazz) throws BeansException {
        return getTypedBean(SpringBootstrap.getContext(), clazz);
    }

    public static <T> T getTypedBean(String name) throws BeansException {
        return getTypedBean(SpringBootstrap.getContext(), name);
    }

    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> getTypedBeans(ApplicationContext context, Class<T> clazz) throws BeansException {
        Map<?, ?> map = context.getBeansOfType(clazz);
        Map<String, T> r = new HashMap<String, T>();
        for (Entry<?, ?> e : map.entrySet()) {
            String beanName = e.getKey().toString();
            T bean = (T) e.getValue();
            r.put(beanName, bean);
        }
        return r;
    }

    public static <T> Map<String, T> getTypedBeans(Class<T> clazz) throws BeansException {
        return getTypedBeans(SpringBootstrap.getContext(), clazz);
    }

    public static <T> T getTypedBeanSilently(Class<T> clazz) {
        try {
            return getTypedBean(clazz);
        } catch (Throwable e) {
            log.info("{}", e.toString());
            return null;
        }
    }

    public static <T> T getTypedBeanSilently(String name) {
        try {
            return getTypedBean(name);
        } catch (Throwable e) {
            log.info("{}", e.toString());
            return null;
        }
    }

    public static <T> Map<String, T> getTypedBeansSilently(Class<T> clazz) {
        try {
            return getTypedBeans(clazz);
        } catch (Throwable e) {
            log.info("{}", e.toString());
            return null;
        }
    }

    /**
     * <pre>
     * �жϵ�ǰClass�Ƿ��ǻ�������
     * 
     * ���ٲ���org.springframework.util.ClassUtils
     * @param clazz
     * @return
     */
    public static boolean isBasicType(Type clazz) {
        return basicTypes.contains(clazz);
    }

    /**
     * �жϵ�ǰClass�Ƿ��ǻ�������
     * 
     * @param clazz
     * @return
     */
    public static boolean isNotBasicType(Type clazz) {
        return !isBasicType(clazz);
    }
}
