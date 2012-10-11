package com.xunlei.util.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * �̹߳����࣬�ṩ�����̵߳Ĺ���
 * 
 * @author ZengDong
 * @since 2010-9-30 ����01:58:30
 */
public class NamedThreadFactory implements ThreadFactory {

    /**
     * �߳���
     */
    protected final ThreadGroup group;
    /**
     * ��֤ԭ�Ӳ���������
     */
    protected final AtomicInteger threadNumber = new AtomicInteger(1);
    /**
     * ����ǰ׺
     */
    protected final String namePrefix;
    /**
     * Ĭ�����ȼ�
     */
    protected int priority = Thread.NORM_PRIORITY;
    /**
     * �Ƿ�Ϊ�ػ��߳�
     */
    protected boolean daemon = false;

    /**
     * ���췽��
     * 
     * @param namePrefix
     * @param priority
     * @param daemon
     */
    public NamedThreadFactory(String namePrefix, int priority, boolean daemon) {
        this(namePrefix);
        this.daemon = daemon;
        this.priority = priority;
    }

    /**
     * ���췽��
     * 
     * @param namePrefix
     * @param priority
     */
    public NamedThreadFactory(String namePrefix, int priority) {
        this(namePrefix);
        this.priority = priority;
    }

    /**
     * ���췽��
     * 
     * @param namePrefix
     */
    public NamedThreadFactory(String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    /**
     * ����һ���µ��߳�
     */
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        t.setDaemon(daemon);
        t.setPriority(priority);
        return t;
    }

    public String getNamePrefix() {
        return namePrefix;
    }
}
