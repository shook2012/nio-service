package com.xunlei.util.concurrent;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ֧�ֲ�����HashSet ����ɽկ�취ʵ�ֵ�֧�ֲ�����HashSet
 * 
 * @since 2010-9-18
 * @author hujiachao
 */
public class ConcurrentHashSet<T> extends AbstractSet<T> implements Serializable {

    private static final long serialVersionUID = -354041681348976608L;
    /**
     * ��hashset�еĶ���ŵ�map��
     */
    private Map<T, Boolean> map;
    /**
     * ���ϵ�Ĭ�ϴ�С
     */
    private static final int DefaultCapacity = 16;

    /**
     * Ĭ�Ϲ��췽��
     */
    public ConcurrentHashSet() {
        this(DefaultCapacity);
    }

    /**
     * ָ�����ϴ�С�Ĺ��췽��
     * 
     * @param initailCapacity
     */
    public ConcurrentHashSet(int initailCapacity) {
        map = new ConcurrentHashMap<T, Boolean>(initailCapacity);
    }

    /**
     * ��õ�����
     */
    @Override
    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }

    /**
     * ��ǰ������Ԫ�ص�����
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * ��ǰ�������Ƿ�����ض��Ķ���
     */
    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    /**
     * �ڼ����м����µ�Ԫ��
     */
    @Override
    public boolean add(T o) {
        Boolean answer = ((ConcurrentHashMap<T, Boolean>) map).putIfAbsent(o, Boolean.TRUE);
        return answer == null;
    }

    /**
     * ɾ�������е�Ԫ��
     */
    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    /**
     * ��ռ����е�����Ԫ��
     */
    @Override
    public void clear() {
        map.clear();
    }
}
