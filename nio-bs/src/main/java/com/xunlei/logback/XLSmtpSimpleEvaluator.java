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
 * �жϵ�ǰ�ۻ�ͳ�Ƶ���log�¼�����һ����(Ĭ��256,��Ҫ��Ϊ�˸�cyclicBufferTrackerĬ��ֵͳһ)/ͳ�Ƶ�ǰ���ϴη����ʼ�ʱ�䳬��n���Ӻ�,���ʼ�(Ĭ��20����)
 * 
 * �˴�����Ƚ�ԭʼ��,��Ҫ���ǣ�
 * 1.��ͬ������¼�,counter�ۼ�����ͬ,��error����һ�μ�10,��info��2 [��δʵ��]
 * 
 * 2.��ǰʵ�ֻ����һ�����������10���¼�,��10���¼�������20�����ڷ�����,���ڳ��������������,ֻҪ��û�з�����11���¼�,�ʼ������ᱻ����
 * 
 * Ҫ���������,����ͨ��һ�� ����Ķ�ʱɨ��������©�� 
 * ���ͨ��initDaemonScanner()��ʵ��(���Ǿ�ȷʵʱ��,���Ѿ�����Ҫ��)
 * 
 * 3.�ڳ���ر�ʱ,Ҳ���������2�����
 * 
 * @author ZengDong
 * @since 2011-3-12 ����02:31:37
 */
public class XLSmtpSimpleEvaluator extends EventEvaluatorBase<ILoggingEvent> {

    /**
     * �ϴ�ˢ���¼�
     */
    private long lastFlushTime = 0;
    /**
     * ǿ�ƴ����¼�ʱʹ�õ�loggerName
     */
    private String relevanceLoggerName;// �ں�̨��ʱɨ������������©ʱ,ʹ�ô�loggerName��ǿ�ƴ����¼�
    /**
     * ��־������
     */
    private AtomicInteger counter = new AtomicInteger();
    /**
     * Ĭ�ϵ������־����
     */
    private int eventNumLimit = 256;// Ĭ�ϸ�cyclicBufferTrackerĬ��ֵͳһ
    /**
     * ������־�¼�����Ƶ�ʵ�ʱ���
     */
    private int eventSecondLimit = 20 * 60; // Ĭ��20����
    /**
     * ��eventSecondLimitת��Ϊ������
     */
    private int _eventTimeLimit = eventSecondLimit * 1000;
    /**
     * Ϊ�˽��logbackɨ�赽�����ļ��и���ʱ,���ؽ�vo,ͨ��ָ����������ʶ
     */
    private String name;// Ϊ�˽��logbackɨ�赽�����ļ��и���ʱ,���ؽ�vo,ͨ��ָ����������ʶ
    /**
     * ��¼����ע�ᵽ��XLSmtpSimpleEvaluator
     */
    private static final Map<String, XLSmtpSimpleEvaluator> evaluatorMap = new HashMap<String, XLSmtpSimpleEvaluator>(0);// �����¼������ע�ᵽ��XLSmtpSimpleEvaluator
    /**
     * �Ƿ�ǿ�ƴ���
     */
    private volatile boolean forceTrigger = false;
    private volatile String latestMailTitle = "";

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                for (XLSmtpSimpleEvaluator ev : evaluatorMap.values()) {
                    ev.trigger();// ֻҪ��������һ������
                }
            }
        });// ���������˳�ʱ�ഥ��

        ConcurrentUtil.getDaemonExecutor().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                if (evaluatorMap.isEmpty()) {// ��һ�����ڶ�û���ҵ��κ�ע���XLSmtpSimpleEvaluator,�����ʱ��ȡ��
                    String msg = "cant find any XLSmtpSimpleEvaluator,stop its DaemonScanner";
                    System.err.println(msg);
                    throw new RuntimeException(msg);
                }
                for (XLSmtpSimpleEvaluator ev : evaluatorMap.values()) {
                    ev.check();
                }
            }
        }, 1, 1, TimeUnit.MINUTES);// ÿ���Ӷ��ڼ��

    }

    /**
     * �����¼�
     */
    private void trigger() {
        if (counter.get() > 0) { // counter> 0�������,relevanceLoggerNameһ����null
            forceTrigger = true;
            MDC.put("mailTitle", latestMailTitle);
            Log.getLogger(relevanceLoggerName).error("XLSmtpSimpleEvaluator.DaemonScanner trigger");
        }
    }

    /**
     * �������ϴδ���ʱ���Ƿ񳬹���ָ����ʱ��Σ���������ʹ����¼�
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
     * �Ե�ǰ�������������⣬����������¼����Ҵﵽ���¼����������ͬʱ������ʱ��Σ�������������
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
     * �����ʼ�������
     * 
     * @param now
     */
    private void reset(long now) {
        forceTrigger = false;
        counter.set(0);
        lastFlushTime = now;
    }

    /**
     * ����¼�������������
     * 
     * @return
     */
    public int getEventNumLimit() {
        return eventNumLimit;
    }

    /**
     * �����¼�������������
     * 
     * @param eventNumLimit
     */
    public void setEventNumLimit(int eventNumLimit) {
        this.eventNumLimit = eventNumLimit;
    }

    /**
     * �������ʱʹ�õ�ʱ���
     * 
     * @return
     */
    public int getEventSecondLimit() {
        return eventSecondLimit;
    }

    /**
     * ����ʱ���
     * 
     * @param eventSecondLimit
     */
    public void setEventSecondLimit(int eventSecondLimit) {
        this.eventSecondLimit = eventSecondLimit;
        this._eventTimeLimit = eventSecondLimit * 1000;
    }

    /**
     * ��������
     */
    @Override
    public void setName(String name) {
        this.name = name;
        evaluatorMap.put(name, this);// ע��
    }

    /**
     * �������
     */
    @Override
    public String getName() {
        return name;
    }
}
