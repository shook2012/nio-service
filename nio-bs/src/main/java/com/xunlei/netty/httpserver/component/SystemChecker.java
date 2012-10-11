package com.xunlei.netty.httpserver.component;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xunlei.netty.httpserver.util.HttpServerConfig;
import com.xunlei.spring.AfterBootstrap;
import com.xunlei.spring.Config;
import com.xunlei.util.Log;
import com.xunlei.util.SystemInfo;
import com.xunlei.util.SystemMonitor;
import com.xunlei.util.concurrent.ConcurrentUtil;

@Service
public class SystemChecker {

    private static final Logger log = Log.getLogger(SystemMonitor.class);
    @Autowired
    protected HttpServerConfig config;
    private static volatile boolean denialOfService = false;
    /** ϵͳ����Ҫ���̳߳صļ�أ�Ҫ��ʵʱ */
    @Config
    private int dosMonitorCheckSec = 1;
    /** �������ռ�أ�����5���Ӽ��һ�Σ���Ϊ���ܻ���ʱ��ĶԱȣ� */
    @Config
    private int gcMonitorSec = 5 * 60;
    private static volatile boolean lastTestDenialOfService = false;
    @Config(resetable = true)
    private double loadMonitorErrorThreshold = ConcurrentUtil.CORE_PROCESSOR_NUM;
    /** ϵͳ���ؼ�أ��������Ӽ��һ�Σ���Ϊ��ƽ�����أ� */
    @Config
    private int loadMonitorSec = 1 * 30;
    /** ��ֵ�Ľ綨����ο�http://blog.csdn.net/marising/article/details/5182771 */
    @Config(resetable = true)
    private double loadMonitorWarnThreshold = ConcurrentUtil.CORE_PROCESSOR_NUM * 0.7;
    /** �߳�cpuʱ����,����ÿ����Ӽ��һ�� */
    @Config
    private int threadCpuTimeMonitorSec = 1 * 30;
    @Config(resetable = true)
    private int threadMonitorQueueThreshold = 1000;
    /** �̳߳ؼ�أ�����ÿ5���Ӽ��һ�Σ���Ϊÿʱÿ�̵�queue�������������ڴ���ȱ仯�� */
    @Config
    private int threadMonitorSec = 5;
    /** log�̳߳ؼ�أ�Ҫ�Ϸ����й�����IO���ߺ��ڴ���� */
    @Config
    private int logMonitorCheckSec = 10;
    @Config(resetable = true)
    private int logMonitorLimit = 20000;
    private static volatile boolean lastTestLog = false;
    private static volatile boolean logEnabled = true;

    @AfterBootstrap
    protected void init() {
        SystemMonitor.initGarbageCollectMonitor(gcMonitorSec);
        SystemMonitor.initLoadAverageMonitor(loadMonitorSec, loadMonitorWarnThreshold, loadMonitorErrorThreshold);
        SystemMonitor.initThreadCpuTimeMonitor(threadCpuTimeMonitorSec);
        SystemMonitor.initThreadPoolMonitor(threadMonitorSec, threadMonitorQueueThreshold);
        initDenialOfServiceMonitor();
        initLogMonitor();
    }

    public void initDenialOfServiceMonitor() {
        if (dosMonitorCheckSec > 0) {
            log.info("DenialOfServiceMonitor ON, interval:{}sec, pipelineSize:{}", dosMonitorCheckSec, config.getPipelineExecutorOrdered().getCorePoolSize());
            ConcurrentUtil.getWatchdog().scheduleWithFixedDelay(new Runnable() {

                private boolean check(ThreadPoolExecutor executor) {
                    int activeCount = executor.getActiveCount();
                    int largestPoolSize = executor.getLargestPoolSize();
                    int atLeastReamin = largestPoolSize <= HttpServerConfig.CORE_PROCESSOR_NUM ? 1 : HttpServerConfig.CORE_PROCESSOR_NUM; // ����Ҫʣ��Ŀ����߳���
                    if (activeCount > 0 && activeCount + atLeastReamin >= largestPoolSize) {
                        String pipelineInfo = SystemInfo.getThreadsDetailInfo("PIPELINE", true, 20); // �ȴ�ӡ��
                        activeCount = executor.getActiveCount();
                        if (activeCount > 0 && activeCount + atLeastReamin >= largestPoolSize) { // ��ӡ���ˣ��̳߳���Ȼ����ռ�ã��ͷ��������ʼ�
                            if (!lastTestDenialOfService) {
                                MDC.put("mailTitle", "DenialOfService");
                                log.error("DENIAL OF SERVICE,WAITTING_QUEUE_SIZE:{},RUNNING PIPELINE:\n{}\n\n", activeCount, pipelineInfo);
                            }
                            denialOfService = true;
                            lastTestDenialOfService = true;
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void run() {
                    boolean r = check(config.getPipelineExecutorOrdered());
                    boolean r1 = check(config.getPipelineExecutorUnordered());
                    if (!r && !r1) {
                        denialOfService = false;
                        lastTestDenialOfService = false;
                    }
                }
            }, dosMonitorCheckSec * 5, dosMonitorCheckSec, TimeUnit.SECONDS);// httpServer������ʱ�����п��ܺܶ������������Ȳ��ܾ����������ݶ�5s���ٿ�ʼ��ʱ
        }
    }

    public void initLogMonitor() {
        if (logMonitorCheckSec > 0) {
            final ThreadPoolExecutor logExecutor = (ThreadPoolExecutor) ConcurrentUtil.getLogExecutor();
            log.info("LogMonitor ON, interval:{}sec, logExecutorSize:{}", logMonitorCheckSec, logExecutor.getQueue().size());
            ConcurrentUtil.getWatchdog().scheduleWithFixedDelay(new Runnable() {

                private boolean check(ThreadPoolExecutor executor) {
                    int queueCount = executor.getQueue().size(); // ������е�log�������ƣ�����ʱ����־���ܹر�
                    if (queueCount > logMonitorLimit) {
                        if (!lastTestLog) {
                            MDC.put("mailTitle", "TOO MANY LOGS");
                            log.error("TOO MANY LOGS,WAITTING_QUEUE_SIZE:{}\n", queueCount);
                        }
                        logEnabled = false;
                        lastTestLog = true;
                        return true;
                    }
                    return false;
                }

                @Override
                public void run() {
                    boolean r = check(logExecutor);
                    if (!r) {
                        logEnabled = true;
                        lastTestLog = false;
                    }
                }
            }, logMonitorCheckSec, logMonitorCheckSec, TimeUnit.SECONDS);
        }
    }

    public static boolean isDenialOfService() {
        return denialOfService;
    }

    /**
     * �����־���ܴ��ˣ��ż�¼��־
     */
    public static boolean isLogEnabled() {
        return logEnabled;
    }
}
