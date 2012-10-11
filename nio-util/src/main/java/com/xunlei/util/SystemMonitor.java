package com.xunlei.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.MDC;
import com.xunlei.util.concurrent.ConcurrentUtil;

/**
 * ϵͳ��أ���Ҫ���gc,threadPool,loadAverage
 * 
 * @author ZengDong
 * @since 2011-12-16 ����11:49:24
 */
public class SystemMonitor {

    private SystemMonitor() {
    }

    private static final Logger log = Log.getLogger();
    private static final Logger log_loadaverage = Log.getLoggerWithSuffix("loadaverage");
    private static Map<Long, Long> calcThreadsCpuTime;

    // TODO:��ʱû�мӹص���ʱ������߼�
    /**
     * �߳�cpuʱ����,����ÿ����Ӽ��һ��
     */
    public static boolean initThreadCpuTimeMonitor(int interval) {
        if (interval <= 0) {
            return false;
        }

        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        boolean cpuTimeEnabled = false;
        try {
            cpuTimeEnabled = threadMXBean.isThreadCpuTimeEnabled();// �����Ƿ��������߳� CPU ʱ�������
        } catch (UnsupportedOperationException e) {
        }
        boolean cpuTimeSupported = threadMXBean.isThreadCpuTimeSupported();// ���� Java �����ʵ���Ƿ�֧���κ��̵߳� CPU ʱ�������֧���κ��߳� CPU ʱ��ⶨ�� Java �����ʵ��Ҳ֧�ֵ�ǰ�̵߳� CPU ʱ��ⶨ��

        if (cpuTimeEnabled && cpuTimeSupported) {
            log.info("ThreadCpuTimeMonitor    ON,interval:{}sec", new Object[] { interval });
            ConcurrentUtil.getWatchdog().scheduleWithFixedDelay(new Runnable() {

                private Map<Long, Long> lastAllTime;

                @Override
                public void run() {
                    try {
                        boolean printInfo = false;
                        Map<Long, Long> thisAllTime = new HashMap<Long, Long>();
                        long[] ids = threadMXBean.getAllThreadIds();
                        Map<Long, Long> calcThreadsCpuTimeTmp = new HashMap<Long, Long>();

                        for (long id : ids) {
                            long time = threadMXBean.getThreadCpuTime(id);
                            thisAllTime.put(id, time);
                            long lastTime = 0;
                            if (lastAllTime != null) {
                                Long tmp = lastAllTime.get(id);
                                if (tmp != null) {
                                    lastTime = tmp;
                                }
                            }
                            calcThreadsCpuTimeTmp.put(id, time - lastTime);
                        }
                        calcThreadsCpuTime = calcThreadsCpuTimeTmp;
                        lastAllTime = thisAllTime;

                        // ������ ��ʱ��ӡ�߳�cputimeǰ10���������Ȳ���
                        if (printInfo && log.isInfoEnabled()) {
                            // http://chenchendefeng.iteye.com/blog/561649
                            List<Map.Entry<Long, Long>> sort = new ArrayList<Map.Entry<Long, Long>>(calcThreadsCpuTimeTmp.entrySet());
                            Collections.sort(sort, new Comparator<Map.Entry<Long, Long>>() {

                                @Override
                                public int compare(Map.Entry<Long, Long> o1, Map.Entry<Long, Long> o2) {
                                    int r = (int) (o2.getValue() - o1.getValue());
                                    return r == 0 ? (int) (o2.getKey() - o1.getKey()) : r;
                                }
                            });
                            if (sort.size() > 0 && sort.get(0).getValue() > 0) {// û�в����Ͳ���ʾ��
                                StringBuilder tmp = new StringBuilder();
                                String fmt = "%-8s%-14s%-8s%-8s%-22s%-22s%s\n";
                                tmp.append(String.format(fmt, "ID", "STATE ", "Blocks", "Waits", "CpuTime", "LastCpuTime", "Name"));
                                for (int i = 0; i < 10; i++) {// ��ӡǰʮ��ռ��cputime�߳�
                                    Entry<Long, Long> e = sort.get(i);
                                    Long id = e.getKey();
                                    Long time = e.getValue();
                                    ThreadInfo info = threadMXBean.getThreadInfo(id);
                                    long cupTime = threadMXBean.getThreadCpuTime(id);

                                    String lastCpuTime = time + "";
                                    tmp.append(String.format(fmt, id, info.getThreadState(), info.getBlockedCount(), info.getWaitedCount(), HumanReadableUtil.timeSpan(cupTime / 1000), lastCpuTime,
                                            info.getThreadName()));
                                }
                                log.info("\n{}", tmp);
                            }
                        }

                    } catch (Throwable e) {
                        log.info("", e);
                    }

                }
            }, interval, interval, TimeUnit.SECONDS);
            return true;
        }
        log.info("ThreadCpuTimeMonitor     OFF,cpuTimeSupported{},cpuTimeEnabled:{}", cpuTimeSupported, cpuTimeEnabled);
        return false;
    }

    /**
     * ϵͳ���ؼ�أ�����ÿ����Ӽ��һ�Σ���Ϊ��ƽ�����أ���cpu*0.7 , cpu*1
     */
    public static boolean initLoadAverageMonitor(int interval, final double warnThreshold, final double errorThreshold) {
        // http: // blog.csdn.net/marising/article/details/5182771
        if (interval <= 0) {
            return false;
        }
        final OperatingSystemMXBean mx = ManagementFactory.getOperatingSystemMXBean();
        if (mx.getSystemLoadAverage() < 0) {
            log.info("LoadAverageMonitor      OFF,because jvm can't stat loadaverage in os:{}/arch:{}", mx.getName(), mx.getArch());
            return false;
        }
        log.info("LoadAverageMonitor      ON,interval:{}sec,warnThreshold:{},errorThreshold:{}", new Object[] { interval, warnThreshold, errorThreshold });
        ConcurrentUtil.getWatchdog().scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {
                try {
                    double load = mx.getSystemLoadAverage();
                    if (load < warnThreshold) {
                        log_loadaverage.info("load average:{}", load);
                    } else if (load >= errorThreshold) {
                        MDC.put("mailTitle", "LOAD AVERAGE:" + load);
                        log_loadaverage.error("load average:{}", load);
                    } else {
                        MDC.put("mailTitle", "LOAD AVERAGE:" + load);
                        log_loadaverage.warn("load average:{}", load);
                    }
                } catch (Throwable e) {
                    log.info("", e);
                }

            }
        }, interval, interval, TimeUnit.SECONDS);
        return true;
    }

    /**
     * �������ռ�أ�����5���Ӽ��һ�Σ���Ϊ���ܻ���ʱ��ĶԱȣ�
     */
    public static boolean initGarbageCollectMonitor(int interval) {
        if (interval <= 0) {
            return false;
        }
        List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
        if (gcs.size() == 2) {// TODO:��ʱ����gcΪ���������
            final GarbageCollectorMXBean youngGc = gcs.get(0);
            final GarbageCollectorMXBean fullGc = gcs.get(1);
            if (youngGc.getMemoryPoolNames().length < fullGc.getMemoryPoolNames().length) {// TODO:��ʱ��ΪfullGc��memoryPoolName
                log.info("GarbageCollectMonitor   ON,interval:{}sec,yongGc:{},fullGc:{}", new Object[] { interval, youngGc.getName(), fullGc.getName() });
                ConcurrentUtil.getWatchdog().scheduleWithFixedDelay(new Runnable() {

                    private long last;// ��¼�ϴε�fullGc���������ڹ����ظ������������
                    private long lastGap = 0; // �ϴε�FullGC��YoungGC��ʱ���

                    @Override
                    public void run() {
                        try {
                            long young = youngGc.getCollectionTime();
                            long full = fullGc.getCollectionTime();
                            long thisFull = fullGc.getCollectionCount();
                            if (last != thisFull && full - young > lastGap) { // ���fullGC����ʱ���yongGCʱ�仹�ߣ���Ҫ����������֮�����ʱ���һ������ͼ�����
                                StringBuilder sb = new StringBuilder();
                                SystemInfo.getMemoryAndGcInfo(sb);
                                MDC.put("mailTitle", "GC WARNNING");
                                log.error("GC WARNNING:{}", sb);
                                last = thisFull;
                                lastGap = full - young;
                            }
                        } catch (Throwable e) {
                            log.info("", e);
                        }

                    }
                }, interval, interval, TimeUnit.SECONDS);
                return true;
            }
            log.info("GarbageCollectMonitor   OFF,because yongGc.memoryPool:[{}] < fullGc.memroyPool:{}", Arrays.toString(youngGc.getMemoryPoolNames()), Arrays.toString(fullGc.getMemoryPoolNames()));
        } else {
            StringBuilder sb = new StringBuilder();
            int i = 1;
            for (GarbageCollectorMXBean gc : gcs) {
                sb.append(i++).append(":").append(gc.getName()).append(";");
            }
            log.info("GarbageCollectMonitor   OFF,because garbageCollectorMXBeans.size<>2:{}", sb);
        }
        return false;
    }

    /**
     * �̳߳ؼ�أ�����ÿ5���Ӽ��һ�Σ���Ϊÿʱÿ�̵�queue�������������ڴ���ȱ仯��
     */
    public static boolean initThreadPoolMonitor(int interval, final int queueThreshold, final ExecutorService... executorServices) {
        if (interval <= 0 || queueThreshold <= 0 || EmptyChecker.isEmpty(executorServices)) {
            return false;
        }
        log.info("ThreadPoolMonitor       ON,interval:{}sec,queueThreshold:{},checkExecutors:{}", new Object[] { interval, queueThreshold, executorServices.length });
        ConcurrentUtil.getWatchdog().scheduleWithFixedDelay(new Runnable() {

            @Override
            public void run() {// TODO:���Ըĳ�ע����ʽ
                try {
                    for (ExecutorService e : ConcurrentUtil.getAllExecutors(executorServices)) {
                        watch(e);
                    }
                } catch (Throwable e) {
                    log.info("", e);
                }
            }

            private void watch(ExecutorService e) {
                if (e instanceof ThreadPoolExecutor) {
                    ThreadPoolExecutor executor = (ThreadPoolExecutor) e;
                    if (executor.getQueue().size() > queueThreshold) {
                        String executorName = ConcurrentUtil.getExecutorName(e);
                        MDC.put("mailTitle", "ThreadPool WARNNING" + executorName);

                        if (ConcurrentUtil.getNamedExecutorThreadFactory(e) == null) {
                            log.error("ThreadPool WARNNING:{}\n", ConcurrentUtil.getExecutorInfo(e));
                        } else {
                            String threadInfo = ConcurrentUtil.getNamedExecutorThreadFactory(e) == null ? null : SystemInfo.getThreadsDetailInfo(executorName, true, 20);
                            if (executor.getQueue().size() > queueThreshold) {
                                log.error("ThreadPool WARNNING:\n{}THREADS_INFO:\n{}\n\n", ConcurrentUtil.getExecutorInfo(e), threadInfo);
                            }
                        }
                    }
                }
            }
        }, interval, interval, TimeUnit.SECONDS);
        return true;
    }

    public static Map<Long, Long> getCalcThreadsCpuTime() {
        return calcThreadsCpuTime;
    }

}
