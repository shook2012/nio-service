package com.xunlei.logback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.MDC;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import com.xunlei.util.Log;
import com.xunlei.util.concurrent.ConcurrentUtil;

/**
 * <pre>
 * 判断当前累积统计到的log事件超过一定量(默认256,主要是为了跟cyclicBufferTracker默认值统一)/统计当前离上次发送邮件时间超过n秒钟后,则发邮件(默认20分钟)
 * 
 * 此触发类比较原始简单,需要考虑：
 * 1.不同级别的事件,counter累加量不同,如error级别一次加10,而info加2 [暂未实现]
 * 
 * 2.当前实现会出现一种情况：发了10个事件,这10个事件都是在20分钟内发生的,但在程序的生命周期内,只要其没有发生第11个事件,邮件都不会被触发
 * 
 * 要解决此问题,必须通过一个 额外的定时扫描器来补漏， 
 * 因此通过initDaemonScanner()来实现(不是精确实时的,但已经满足要求)
 * 
 * 3.在程序关闭时,也会出现类似2的情况
 * 
 * @author ZengDong
 * @since 2011-3-12 下午02:31:37
 */
public class XLSmtpSimpleEvaluator extends EventEvaluatorBase<ILoggingEvent> {

    /**
     * 上次刷新事件
     */
    private long lastFlushTime = 0;
    /**
     * 强制触发事件时使用的loggerName
     */
    private String relevanceLoggerName;// 在后台定时扫描器发现有遗漏时,使用此loggerName来强制触发事件
    /**
     * 日志的数量
     */
    private AtomicInteger counter = new AtomicInteger();
    /**
     * 默认的最大日志数量
     */
    private int eventNumLimit = 256;// 默认跟cyclicBufferTracker默认值统一
    /**
     * 计算日志事件发生频率的时间段
     */
    private int eventSecondLimit = 20 * 60; // 默认20分钟
    /**
     * 将eventSecondLimit转化为毫秒数
     */
    private int _eventTimeLimit = eventSecondLimit * 1000;
    /**
     * 为了解决logback扫描到配置文件有更新时,会重建vo,通过指定名字来标识
     */
    private String name;// 为了解决logback扫描到配置文件有更新时,会重建vo,通过指定名字来标识
    /**
     * 记录所有注册到的XLSmtpSimpleEvaluator
     */
    private static final Map<String, XLSmtpSimpleEvaluator> evaluatorMap = new HashMap<String, XLSmtpSimpleEvaluator>(0);// 这里记录下所有注册到的XLSmtpSimpleEvaluator
    /**
     * 是否强制触发
     */
    private volatile boolean forceTrigger = false;
    private volatile String latestMailTitle = "";

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                for (XLSmtpSimpleEvaluator ev : evaluatorMap.values()) {
                    ev.trigger();// 只要有遗留就一定触发
                }
            }
        });// 程序正常退出时亦触发

        ConcurrentUtil.getDaemonExecutor().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if (evaluatorMap.isEmpty()) {// 在一分钟内都没有找到任何注册的XLSmtpSimpleEvaluator,这个定时器取消
                    String msg = "cant find any XLSmtpSimpleEvaluator,stop its DaemonScanner";
                    System.err.println(msg);
                    throw new RuntimeException(msg);
                }
                for (XLSmtpSimpleEvaluator ev : evaluatorMap.values()) {
                    ev.check();
                }
            }
        }, 1, 1, TimeUnit.MINUTES);// 每分钟定期检查

    }

    /**
     * 触发事件
     */
    private void trigger() {
        if (counter.get() > 0) { // counter> 0的情况下,relevanceLoggerName一定非null
            forceTrigger = true;
            MDC.put("mailTitle", latestMailTitle);
            Log.getLogger(relevanceLoggerName).error("XLSmtpSimpleEvaluator.DaemonScanner trigger");
        }
    }

    /**
     * 检查距离上次触发时间是否超过了指定的时间段，如果超过就触发事件
     */
    private void check() {
        try {
            if (System.currentTimeMillis() - lastFlushTime > _eventTimeLimit) {
                trigger();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 对当前评测器进行评测，如果允许触发事件并且达到了事件的最大数量同时超过了时间段，则重置评测器
     */
    @Override
    public boolean evaluate(ILoggingEvent e) throws EvaluationException {
        long now = e.getTimeStamp();
        if (lastFlushTime == 0) {
            lastFlushTime = now;
            relevanceLoggerName = e.getLoggerName();
        }
        latestMailTitle = MDC.get("mailTitle");
        if (forceTrigger || counter.incrementAndGet() >= eventNumLimit || now - lastFlushTime > _eventTimeLimit) {
            reset(now);
            return true;
        }
        return false;
    }

    /**
     * 重置邮件评测器
     * 
     * @param now
     */
    private void reset(long now) {
        forceTrigger = false;
        counter.set(0);
        lastFlushTime = now;
    }

    /**
     * 获得事件允许的最大数量
     * 
     * @return
     */
    public int getEventNumLimit() {
        return eventNumLimit;
    }

    /**
     * 设置事件允许的最大数量
     * 
     * @param eventNumLimit
     */
    public void setEventNumLimit(int eventNumLimit) {
        this.eventNumLimit = eventNumLimit;
    }

    /**
     * 获得评测时使用的时间段
     * 
     * @return
     */
    public int getEventSecondLimit() {
        return eventSecondLimit;
    }

    /**
     * 设置时间段
     * 
     * @param eventSecondLimit
     */
    public void setEventSecondLimit(int eventSecondLimit) {
        this.eventSecondLimit = eventSecondLimit;
        this._eventTimeLimit = eventSecondLimit * 1000;
    }

    /**
     * 设置名字
     */
    @Override
    public void setName(String name) {
        this.name = name;
        evaluatorMap.put(name, this);// 注册
    }

    /**
     * 获得名字
     */
    @Override
    public String getName() {
        return name;
    }
}
