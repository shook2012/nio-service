package com.xunlei.util.concurrent;

import java.util.Date;
import org.slf4j.Logger;
import com.xunlei.logback.FixNameMemAppender;
import com.xunlei.logback.FixNameMemAppender.FixNameLog;
import com.xunlei.logback.FixSizeMemAppender.FixSizeLog;
import com.xunlei.util.DateStringUtil;
import com.xunlei.util.HumanReadableUtil;

/**
 * 请注意，此类是线程不安全的，需要外部来包装成线程安全类，如BaseCmdJob
 * 
 * @author ZengDong
 * @since 2011-12-12 下午9:32:46
 */
public abstract class BaseJob implements Runnable, FixNameLog {

    public static int DEFAULT_LOG_SIZE = 0;
    protected String jobName;
    protected String description;
    private FixSizeLog lastRunningInfo;
    private Date lastStartTime = null;
    protected Logger log;
    private boolean logAppnederInited = false;
    protected int logSize;
    private volatile boolean running = false;
    private FixSizeLog runningInfo;
    private Thread runningThread;
    protected int runTimes;
    private boolean scheduleNext;// 是否紧接着触发下次的任务
    private volatile boolean stop = true;

    public BaseJob(Logger log) {
        this(log, log.getName(), log.getName(), DEFAULT_LOG_SIZE);// 不启用日志记录
    }

    public BaseJob(Logger log, int logSize) {
        this(log, log.getName(), log.getName(), logSize);// 一般情况，把log就当作任务名就行了
    }

    public BaseJob(Logger log, String jobName, int logSize) {
        this(log, jobName, jobName, logSize);
    }

    public BaseJob(Logger log, String jobName, String description, int logSize) {
        this.log = log;
        this.jobName = jobName;
        this.description = description;
        this.logSize = logSize;
        initFixNamememAppender();// 如果logSize会在运行变化，则要在变化时，考虑 问题
    }

    private void initFixNamememAppender() {
        if (logSize > 0 && !logAppnederInited) {
            FixNameMemAppender.register(this);
            logAppnederInited = true;
        }
    }

    private void resetRunningInfo() {
        this.lastRunningInfo = runningInfo;
        this.runningInfo = logSize > 0 ? new FixSizeLog(jobName, logSize) : null;
    }

    public void begin() {
        runningThread = Thread.currentThread();
        boolean urge = scheduleNext;
        this.scheduleNext = false;
        resetRunningInfo();
        running = true;
        stop = false;
        runTimes++;
        Date now = new Date();
        log.info("START JOB [{}({})],RUN TIMES:{}{}", new Object[] { jobName, description, runTimes, urge ? "[URGE]" : "" });
        lastStartTime = now;
    }

    public void cancel() {
        stop = true;
        Thread t = getRunningThread();// 拿引用来避免空指针异常
        if (t != null) {
            t.interrupt();
        }
    }

    public void end() {
        runningThread = null;
        long span = System.currentTimeMillis() - lastStartTime.getTime();
        log.info("END   JOB [{}({})] USING {}", new Object[] { jobName, description, HumanReadableUtil.timeSpan(span) });
        running = false;
        stop = true;
    }

    protected String getRunningInfo(int begin, int end, int tail, boolean last) {
        FixSizeLog fsl = last ? lastRunningInfo : runningInfo;
        if (fsl == null) {
            return "";
        }
        if (tail > 0) {
            return fsl.tail(tail);
        }
        return fsl.sub(begin, end);
    }

    public String getLastStartTimeString() {
        if (lastStartTime == null) {
            return "";
        }
        return DateStringUtil.DEFAULT.format(lastStartTime);
    }

    @Override
    public String getLoggerName() {
        return log.getName();
    }

    public Thread getRunningThread() {
        return runningThread;
    }

    public int getRunTimes() {
        return runTimes;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isStop() {
        return stop;
    }

    @Override
    public void log(String msg) {
        if (runningInfo != null) {
            runningInfo.log(msg);
        }
    }

    public abstract void process() throws Throwable;

    @Override
    public void run() {
        do {
            begin();
            try {
                process();
            } catch (InterruptedException e) {
                log.error("INTERRUPT JOB [{}({})]", new Object[] { jobName, description, e }); // 说明被中断了
            } catch (Throwable e) {
                log.error("", e);
            }
            end();
        } while (scheduleNext);
    }

    @Override
    public String toString() {
        return String.format("[jobName=%s, description=%s, log=%s, runTimes=%s, lastStartTime=%s, scheduleNext=%s]", jobName, description, log, runTimes, getLastStartTimeString(), scheduleNext);
    }

    public void urge() {
        if (running) {
            scheduleNext = true;
        }
    }

    public String getJobName() {
        return jobName;
    }
}
