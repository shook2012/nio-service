package com.xunlei.util.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author 曾东
 * @since 2012-5-18 下午5:26:00
 */
public abstract class BaseSchedulable extends BaseRunnable {

    private ScheduledFuture<?> lastScheduledFuture;
    private ScheduledExecutorService executor;

    public BaseSchedulable() {
        this(null);
    }

    public BaseSchedulable(ScheduledExecutorService scheduledExecutorService) {// 注意默认是 后台线程，在主程序退出时，也会退出
        this.executor = scheduledExecutorService == null ? ConcurrentUtil.getDaemonExecutor() : scheduledExecutorService;
    }

    public boolean cancel() {
        return cancel(true);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        ScheduledFuture<?> last = lastScheduledFuture;
        boolean r = true;// 如果还没有schedule也认为是 cacnel返回true
        if (lastScheduledFuture != null) {
            r = lastScheduledFuture.cancel(true);
        }
        log.error("cancel lastFuture:{}", last != null ? "isCancelled:" + last.isCancelled() : "N/A");
        return r;
    }

    public void schedule(long initialDelay, long period, TimeUnit unit, boolean atFixedRate) {
        ScheduledFuture<?> last = lastScheduledFuture;
        if (lastScheduledFuture != null) {
            lastScheduledFuture.cancel(true);
        }
        if (atFixedRate) {
            lastScheduledFuture = executor.scheduleAtFixedRate(this, initialDelay, period, unit);
        } else {
            lastScheduledFuture = executor.scheduleWithFixedDelay(this, initialDelay, period, unit);
        }
        log.info("schedule initialDelay:{},period:{},unit:{},atFixRate:{},lastFuture:{}", new Object[] { initialDelay, period, unit, atFixedRate,
                last != null ? "isCancelled:" + last.isCancelled() : "N/A" });
    }

    public void scheduleWithFixedDelaySec(long sec) {
        this.schedule(sec, sec, TimeUnit.SECONDS, false);
    }

    public void scheduleWithFixedDelayMs(long millsecond) {
        this.schedule(millsecond, millsecond, TimeUnit.MILLISECONDS, false);
    }

    public void scheduleAtFixedRateSec(long sec) {
        this.schedule(sec, sec, TimeUnit.SECONDS, true);
    }

    public void scheduleAtFixedRateMs(long millsecond) {
        this.schedule(millsecond, millsecond, TimeUnit.MILLISECONDS, true);
    }

    public static void main(String[] args) throws InterruptedException {
        BaseSchedulable bs = new BaseSchedulable(Executors.newScheduledThreadPool(1)) {

            @Override
            public void process() throws Throwable {
                System.out.println("gogogo");
            }
        };
        bs.scheduleAtFixedRateSec(1);
        Thread.sleep(10000);
        bs.scheduleAtFixedRateMs(100);
        Thread.sleep(2000);
        bs.scheduleWithFixedDelayMs(500);
    }
}
