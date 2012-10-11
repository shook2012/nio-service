package com.xunlei.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 集合操作工具类
 * 
 * @since 2011-3-23
 * @author hujiachao
 */
public class CollectionUtil {

    /**
     * 批量添加元素到集合中，不检查添加的具体类型，相比Collections.addAll方法不会输出警告
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
     * 生成一个由指定元素组成的List
     */
    public static <T> List<T> buildList(T... ts) {
        List<T> list = new ArrayList<T>(ts.length);
        addAll(list, ts);
        return list;
    }

    /**
     * 生成一个由指定元素组成的Set
     */
    public static <T> Set<T> buildSet(T... ts) {
        Set<T> set = new HashSet<T>(ts.length * 2);
        addAll(set, ts);
        return set;
    }
}
