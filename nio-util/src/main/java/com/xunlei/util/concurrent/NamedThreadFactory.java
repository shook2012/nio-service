package com.xunlei.util.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂类，提供创建线程的功能
 * 
 * @author ZengDong
 * @since 2010-9-30 下午01:58:30
 */
public class NamedThreadFactory implements ThreadFactory {

    /**
     * 线程组
     */
    protected final ThreadGroup group;
    /**
     * 保证原子操作的整数
     */
    protected final AtomicInteger threadNumber = new AtomicInteger(1);
    /**
     * 名字前缀
     */
    protected final String namePrefix;
    /**
     * 默认优先级
     */
    protected int priority = Thread.NORM_PRIORITY;
    /**
     * 是否为守护线程
     */
    protected boolean daemon = false;

    /**
     * 构造方法
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
     * 构造方法
     * 
     * @param namePrefix
     * @param priority
     */
    public NamedThreadFactory(String namePrefix, int priority) {
        this(namePrefix);
        this.priority = priority;
    }

    /**
     * 构造方法
     * 
     * @param namePrefix
     */
    public NamedThreadFactory(String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    /**
     * 创建一个新的线程
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
