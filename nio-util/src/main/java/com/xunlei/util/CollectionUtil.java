package com.xunlei.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ���ϲ���������
 * 
 * @since 2011-3-23
 * @author hujiachao
 */
public class CollectionUtil {

    /**
     * �������Ԫ�ص������У��������ӵľ������ͣ����Collections.addAll���������������
     * 
     * @param c
     * @param objs
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean addAll(Collection c, Object... objs) {
        boolean result = false;
        for (Object obj : objs) {
            result |= c.add(obj);
        }
        return result;
    }

    /**
     * ����һ����ָ��Ԫ����ɵ�List
     */
    public static <T> List<T> buildList(T... ts) {
        List<T> list = new ArrayList<T>(ts.length);
        addAll(list, ts);
        return list;
    }

    /**
     * ����һ����ָ��Ԫ����ɵ�Set
     */
    public static <T> Set<T> buildSet(T... ts) {
        Set<T> set = new HashSet<T>(ts.length * 2);
        addAll(set, ts);
        return set;
    }
}
