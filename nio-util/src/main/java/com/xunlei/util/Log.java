package com.xunlei.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * �ṩ�����־��¼���ķ���
 * 
 * @author ZengDong
 */
public class Log {

    /**
     * ͨ�����ݵ�ǰ���е��߳�,�ҵ����ô˷�������,�Ӷ������ȷ�Ĵ�������Logger
     * 
     * @return
     */
    public static Logger getLogger() {
        return LoggerFactory.getLogger(java.lang.Thread.currentThread().getStackTrace()[2].getClassName());
    }

    /**
     * ��ô���obj�����Logger,ʹ��getLogger(this)����ö�Ӧ�����logger
     * 
     * @param obj ָ����Ҫ�������Ķ���
     * @return
     */
    public static Logger getLogger(Object obj) {
        if (obj instanceof Class<?>) {
            return LoggerFactory.getLogger((Class<?>) obj);
        }
        return LoggerFactory.getLogger(obj.getClass().getName());
    }

    /**
     * ֱ��ָ��logger����
     * 
     * @param str
     * @return
     */
    public static Logger getLogger(String str) {
        return LoggerFactory.getLogger(str);
    }

    /**
     * ͨ��ָ������ǰ׺�ͺ�׺�����Logger
     * 
     * @param obj ����
     * @param prefix ǰ׺
     * @param suffix ��׺
     * @return
     */
    public static Logger getLoggerWith(Object obj, String prefix, String suffix) {
        return LoggerFactory.getLogger(prefix + "." + obj.getClass().getName() + "." + suffix);
    }

    /**
     * ͨ��ǰ׺�ͺ�׺�����Logger
     * 
     * @param prefix ǰ׺
     * @param suffix ��׺
     * @return
     */
    public static Logger getLoggerWith(String prefix, String suffix) {
        return LoggerFactory.getLogger(prefix + "." + java.lang.Thread.currentThread().getStackTrace()[2].getClassName() + "." + suffix);
    }

    /**
     * ͨ�������ǰ׺�����Logger
     * 
     * @param obj ����
     * @param prefix ǰ׺
     * @return
     */
    public static Logger getLoggerWithPrefix(Object obj, String prefix) {
        return LoggerFactory.getLogger(prefix + "." + obj.getClass().getName());
    }

    /**
     * ͨ��ǰ׺�����Logger
     * 
     * @param prefix ǰ׺
     * @return
     */
    public static Logger getLoggerWithPrefix(String prefix) {
        return LoggerFactory.getLogger(prefix + "." + java.lang.Thread.currentThread().getStackTrace()[2].getClassName());
    }

    /**
     * ͨ������ͺ�׺�����Logger
     * 
     * @param obj ����
     * @param suffix ��׺
     * @return
     */
    public static Logger getLoggerWithSuffix(Object obj, String suffix) {
        return LoggerFactory.getLogger(obj.getClass().getName() + "." + suffix);
    }

    /**
     * ͨ����׺�����Logger
     * 
     * @param suffix ��׺
     * @return
     */
    public static Logger getLoggerWithSuffix(String suffix) {
        return LoggerFactory.getLogger(java.lang.Thread.currentThread().getStackTrace()[2].getClassName() + "." + suffix);
    }
}
