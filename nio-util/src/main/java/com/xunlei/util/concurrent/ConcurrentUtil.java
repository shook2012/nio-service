package com.xunlei.util.concurrent;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import com.xunlei.util.HumanReadableUtil;

/**
 * ҵ���Ͼ�����ͳһʹ�õ�ǰutil����̳߳�
 * 
 * @author ZengDong
 * @since 2011-3-23 ����11:10:42
 */
public class ConcurrentUtil {

    public static final CallerRunsPolicy callerRunsPolicy = new CallerRunsPolicy();
    public static final int CORE_PROCESSOR_NUM = Runtime.getRuntime().availableProcessors();
    private static ScheduledExecutorService daemonExecutor;
    private static ExecutorService defaultExecutor;
    public static final RejectedExecutionHandler discardPolicy = new DiscardPolicy();
    private static final String executorStatFmt = "%-23s %-13s %-12s %-16s %-12s %-12s %-18s %-16s %-16s %-16s %-12s\n";
    private static final String executorStatHeader = String.format(executorStatFmt, "Executor", "activeCount", "poolSize", "largestPoolSize", "queueSize", "taskCount", "completedTaskCount",
            "corePoolSize", "maximumPoolSize", "keepAliveTime", "coreTimeOut");
    private static ExecutorService logExecutor;
    private static final String PREFIX = "";// ConcurrentUtil.class.getSimpleName() +"-";

    private static final ScheduledExecutorService watchdog = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(PREFIX + "Watchdog(Sche)", Thread.NORM_PRIORITY, true));

    private static final Collection<Number> atomicCounterList = new HashSet<Number>();
    public static int atomicCounterScanHour = 2;
    public static int atomicCounterResetThreshold = Integer.MAX_VALUE / 2;
    static {
        // Ϊ��ֹ������������ں�̨�̶߳�ʱnСʱ���ü�����,Ĭ����2Сʱ���ж�һ��
        getDaemonExecutor().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                int integerMax = Integer.MAX_VALUE - atomicCounterResetThreshold;
                long longMax = Long.MAX_VALUE - atomicCounterResetThreshold;
                for (Number n : atomicCounterList) {
                    if (n instanceof AtomicInteger) {
                        AtomicInteger i = (AtomicInteger) n;
                        if (i.get() > integerMax) {
                            i.set(0);
                        }
                    } else if (n instanceof AtomicLong) {
                        AtomicLong i = (AtomicLong) n;
                        if (i.get() > longMax) {
                            i.set(0);
                        }
                    }
                }

            }
        }, atomicCounterScanHour, atomicCounterScanHour, TimeUnit.HOURS);
    }

    public static void main(String[] args) {
        System.out.println(Integer.MAX_VALUE);
        System.out.println(Long.MAX_VALUE);
    }

    public static String getAllExecutorInfo(ExecutorService... executors) {
        return getExecutorInfo(getAllExecutors(executors));
    }

    public static boolean threadSleep(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e1) {
            return false;
        }
    }

    public static ExecutorService[] getAllExecutors(ExecutorService... executors) {
        ExecutorService[] arr = new ExecutorService[4 + executors.length];
        int i = 0;
        arr[i++] = watchdog;
        arr[i++] = daemonExecutor;
        arr[i++] = defaultExecutor;
        arr[i++] = logExecutor;
        for (ExecutorService e : executors) {
            arr[i++] = e;
        }
        return arr;
    }

    /**
     * <pre>
     * ��̨�����̳߳�
     * ע��:������̶߳��Ǻ�̨�߳�,���û�����߳�,����������˳�
     */
    public static ScheduledExecutorService getDaemonExecutor() {
        if (daemonExecutor == null) {
            synchronized (ConcurrentUtil.class) {
                if (daemonExecutor == null) {
                    daemonExecutor = Executors.newScheduledThreadPool(CORE_PROCESSOR_NUM, new NamedThreadFactory(PREFIX + "Daemon(Sche)", Thread.NORM_PRIORITY, true));
                }
            }
        }
        return daemonExecutor;
    }

    public static ExecutorService getDefaultExecutor() {
        if (defaultExecutor == null) {
            synchronized (ConcurrentUtil.class) {
                if (defaultExecutor == null) {
                    defaultExecutor = Executors.newCachedThreadPool(new NamedThreadFactory(PREFIX + "Default", Thread.NORM_PRIORITY));

                    // 2011-12-19 ���·�����ֻ���� ������������� �����µ��̣߳�����������
                    // defaultExecutor = new ThreadPoolExecutor(0, CORE_PROCESSOR_NUM * 50, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(CORE_PROCESSOR_NUM * 1000), new NamedThreadFactory(
                    // PREFIX + "Default", Thread.NORM_PRIORITY), callerRunsPolicy);
                }
            }
        }
        return defaultExecutor;
    }

    public static String getExecutorInfo(ExecutorService... executors) {
        StringBuilder tmp = new StringBuilder();
        tmp.append(executorStatHeader);
        for (ExecutorService e : executors) {
            if (e != null) {
                String executorName = getExecutorName(e);
                if (e instanceof ThreadPoolExecutor) {
                    ThreadPoolExecutor executor = (ThreadPoolExecutor) e;
                    tmp.append(String.format(executorStatFmt, executorName, executor.getActiveCount(), executor.getPoolSize(), executor.getLargestPoolSize(), executor.getQueue().size(),
                            executor.getTaskCount(), executor.getCompletedTaskCount(), executor.getCorePoolSize(), executor.getMaximumPoolSize(),
                            HumanReadableUtil.timeSpan(executor.getKeepAliveTime(TimeUnit.MILLISECONDS)), executor.allowsCoreThreadTimeOut()));
                } else {
                    tmp.append("!!!").append(executorName);
                }
            }
        }
        return tmp.toString();
    }

    public static String getExecutorName(ExecutorService e) {
        NamedThreadFactory tf = getNamedExecutorThreadFactory(e);
        return tf == null ? e.getClass().getSimpleName() : tf.getNamePrefix();
    }

    public static ExecutorService getLogExecutor() {
        if (logExecutor == null) {
            synchronized (ConcurrentUtil.class) {
                if (logExecutor == null) {
                    // ����ԭ�� logback1.0.0������̳߳����ã������������� LinkedBlockingQueue û�����ô�С�����ʵ���ϲ��������Ӷ���ֻ���½���һ��log�߳���������
                    // 0 idle threads, 2 maximum threads, no idle waiting

                    // http://www.blogjava.net/killme2008/archive/2008/09/08/227661.html
                    // һ������£�ArrayBlockingQueue��������LinkedBlockingQueue,����LinkedBlockingQueue���޽�ġ�
                    // ����պ���Ҫ�н�
                    // logExecutor = new ThreadPoolExecutor(0, CORE_PROCESSOR_NUM, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(CORE_PROCESSOR_NUM * 1000), new NamedThreadFactory(PREFIX
                    // + "Log", Thread.NORM_PRIORITY), discardPolicy);
                    // 2011-12-19 ���ֶ�ʧ��־Ҳ�ǲ�����
                    int num = (int) Math.round(Math.sqrt(CORE_PROCESSOR_NUM));
                    logExecutor = new ThreadPoolExecutor(num, num, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(PREFIX + "Log", Thread.MIN_PRIORITY));
                }
            }
        }
        return logExecutor;
    }

    public static NamedThreadFactory getNamedExecutorThreadFactory(ExecutorService e) {
        if (e instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) e;
            ThreadFactory tf = executor.getThreadFactory();
            if (tf instanceof NamedThreadFactory) {
                return (NamedThreadFactory) tf;
            }
        }
        return null;
    }

    /**
     * ��̨ɨ���̣߳���Ҫ����̨�̸߳��뿪�����̳߳�ֻר�����ڿ��Ź�ʽ�ļ��
     */
    public static ScheduledExecutorService getWatchdog() {
        return watchdog;
    }

    public static AtomicInteger newAtomicInteger() {
        AtomicInteger i = new AtomicInteger();
        atomicCounterList.add(i);
        return i;
    }

    public static AtomicLong newAtomicLong() {
        AtomicLong i = new AtomicLong();
        atomicCounterList.add(i);
        return i;
    }

    /**
     * ˽�еĹ��췽��
     */
    private ConcurrentUtil() {
    }
}
