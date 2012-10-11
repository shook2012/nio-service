package com.xunlei.util.concurrent;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.xunlei.util.HumanReadableUtil;

/**
 * <pre>
 * simple micro-benchmarking tool
 * ��׼���ܲ��Թ���
 * 
 * ע��:-serverһ����������ܲ��Խ��
 * </pre>
 * 
 * @author ZengDong
 * @since 2010-6-3 ����10:13:21
 */
public class Bench {

    private class TimeMeasureProxy implements Runnable {

        private CountDownLatch measureLatch;
        private Runnable runnable;

        public TimeMeasureProxy(Runnable runnable, int measurements) {
            this.runnable = runnable;
            this.measureLatch = new CountDownLatch(measurements);
        }

        /**
         * �ȴ�����ִ�����
         */
        public void await() {
            try {
                measureLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            this.runnable.run();
            this.measureLatch.countDown();
        }
    }

    public static int DEFAULT_WARMUPS = 1000;

    /**
     * �����ڴ��ʹ����
     * 
     * @return
     */
    public static long memoryUsed() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    /**
     * ����װ��JVM
     */
    public static void restoreJvm0() {
        final CountDownLatch drained = new CountDownLatch(1);
        try {
            System.gc(); // enqueue finalizable objects
            new Object() {

                @Override
                protected void finalize() {
                    drained.countDown();
                }
            };
            System.gc(); // enqueue detector
            drained.await(); // wait for finalizer queue to drain
            System.gc(); // cleanup finalized objects
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    /**
     * ����װ��JVM
     */
    public static void restoreJvm() {
        int maxRestoreJvmLoops = 10;
        long memUsedPrev = memoryUsed();
        for (int i = 0; i < maxRestoreJvmLoops; i++) {
            System.runFinalization();
            System.gc();

            long memUsedNow = memoryUsed();
            // break early if have no more finalization and get constant mem
            // used
            if ((ManagementFactory.getMemoryMXBean().getObjectPendingFinalizationCount() == 0) && (memUsedNow >= memUsedPrev)) {
                break;
            } else {
                memUsedPrev = memUsedNow;
            }
        }
    }

    /**
     * �Ƿ�Ϊdebugģʽ
     */
    private boolean debug = true;
    /**
     * ���͵ĸ�ʽ
     */
    private DecimalFormat integerFormat = new DecimalFormat("#,##0.000");
    private ExecutorService mainExecutor;
    private int measurements;
    private int threads;
    private ExecutorService warmUpExecutor;
    private int warmupMeasurements = DEFAULT_WARMUPS;
    private int timesPerMeasurements;

    public Bench(int threads, int measurements, int warmupMeasurements, int timesPerMeasurements) {
        this.threads = threads <= 0 ? 1 : threads;
        this.measurements = measurements <= 0 ? 1 : measurements;
        this.warmupMeasurements = warmupMeasurements <= 0 ? DEFAULT_WARMUPS : warmupMeasurements;
        this.timesPerMeasurements = timesPerMeasurements <= 0 ? 1 : timesPerMeasurements;
        warmUpExecutor = Executors.newSingleThreadExecutor();
        mainExecutor = Executors.newFixedThreadPool(this.threads);
        // doWarmUpExecutor();
    }

    /**
     * ���߳� task ִ��times��
     * 
     * @param task �߳�
     * @param executor �̳߳�
     * @param times ִ�еĴ���
     */
    private void _run(Runnable task, ExecutorService executor, int times) {
        if (executor == null || task == null) {
            return;
        }
        TimeMeasureProxy timeMeasureProxy = new TimeMeasureProxy(task, times);
        for (int i = 0; i < times; i++) {
            executor.execute(timeMeasureProxy);
        }
        timeMeasureProxy.await();
    }

    /**
     * ��ĳ�߳�ִ��ָ���Ĵ���������������ѵ�ʱ��
     * 
     * @param label �̵߳ı�ʾ
     * @param task �߳�
     */
    private void doMeasure(String label, Runnable task) {
        restoreJvm();
        long startTime = System.nanoTime();
        _run(task, mainExecutor, measurements);
        printResult(label, startTime);
    }

    /**
     * <pre>
     * ����ʹ�� -XX:+PrintCompilation���ж��Ƿ�����warmup
     * ��-verbose:gc������ʾִ�е�gc��ϸ��Ϣ
     * </pre>
     * 
     * @param task �߳�
     */
    private void doWarmup(Runnable task) {
        restoreJvm();
        long startTime = System.nanoTime();
        _run(task, warmUpExecutor, warmupMeasurements);
    }

    // /**
    // * TODO:�Ƿ��б�Ҫ����
    // */
    // private void doWarmUpExecutor() {
    // long startTime = System.nanoTime();
    // if (debug)
    // System.out.println("\tdoWarmUpExecutor");
    // ExecutorService es = Executors.newFixedThreadPool(2);
    // _run(new Runnable() {
    // public void run() {
    // try {
    // LinkedBlockingQueue<Runnable> queue = new
    // LinkedBlockingQueue<Runnable>();
    // queue.offer(this);
    // queue.peek();
    // queue.add(this);
    // queue.poll();
    // _run(null, null, 0);
    // } catch (Exception e) {
    // }
    // }
    // }, es, 1000);
    // es.shutdownNow();
    // restoreJvm();
    // if (debug)
    // System.out.println("\tdoWarmUpExecutor Using" + (System.nanoTime() -
    // startTime) / 1000000 + " MS");
    // }
    /**
     * ��ʼ����
     */
    public void measure(String label, Runnable task) {
        if (debug) {
            System.out.println("\tSTARTUP WARMUP  " + label);
        }
        doWarmup(task);
        if (debug) {
            System.out.println("\tSTARTUP MEASURE " + label);
        }
        doMeasure(label, task);
    }

    /**
     * ����߳����е�ʱ����
     * 
     * @param label �̱߳�ʶ
     * @param startTime �߳̿�ʼʱ��
     */
    private void printResult(String label, long startTime) {
        long span = System.nanoTime() - startTime;
        double avg = span / measurements / timesPerMeasurements / 1000000.0;
        String avgStr = integerFormat.format(avg) + "ms";

        double total = span / 1000000.0;
        String totalStr = integerFormat.format(total) + "ms";

        double tps = measurements * timesPerMeasurements / (span / 1000000000.0);
        String tpsStr = integerFormat.format(tps);

        System.out.println(String.format("%-30s[avg:%-20s total:%-20s tps:%-20s mem:%-10s]", label, avgStr, totalStr, tpsStr, HumanReadableUtil.byteSize(memoryUsed())));
    }

    /**
     * �ر��̳߳�
     */
    public void shutdown() {
        warmUpExecutor.shutdown();
        mainExecutor.shutdown();
    }

    /**
     * ����̳߳���Ϣ
     */
    @Override
    public String toString() {
        return "Bench [threads=" + threads + ", measurements=" + measurements + ", warmupMeasurements=" + warmupMeasurements + ", timesPerMeasurements=" + timesPerMeasurements + "]";
    }

    /**
     * �����Ƿ�Ϊ����ģʽ
     * 
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
