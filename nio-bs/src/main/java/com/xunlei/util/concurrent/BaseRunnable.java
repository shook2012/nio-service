package com.xunlei.util.concurrent;

import java.text.DateFormat;
import java.util.Date;
import org.slf4j.Logger;
import com.xunlei.util.DateUtil;
import com.xunlei.util.Log;
import com.xunlei.util.StringHelper;
import com.xunlei.util.StringTools;

/**
 * <pre>
 * ���ౣ�����һ��ִ��run()��������Ϣ��
 * 1.��ʼִ��ʱ��
 * 2.ִ����ʱ
 * 3.ִ�г����쳣��ջ
 * 
 * ����������
 * 1.��ʱ����
 * 2.���¼����ӡִ��ʱ����ҵ���߼�
 * </pre>
 * 
 * @author ZengDong
 * @since 2010-9-19 ����04:46:18
 */
public abstract class BaseRunnable implements Runnable {

    /**
     * ���һ��run()�Ŀ�ʼִ��ʱ��
     */
    protected Date lastBeginTime = null;
    /**
     * ���һ��run()�Ľ���ִ��ʱ��
     */
    protected Date lastEndTime = null;
    /**
     * ���һ��run()��ִ��ʱ��
     */
    protected long lastSpan = -1;
    /**
     * ���һ��run()��ִ����ɺ�Ķ�ջ��Ϣ
     */
    protected Throwable lastThrowable = null;
    protected final Logger log = Log.getLogger(this.getClass().getName());
    /**
     * ��־��¼��������
     */
    protected String name = log.getName();

    /**
     * ��ɴ�����¼��־��Ϣ
     */
    public void afterProcess() {
        if (lastThrowable == null) {
            log.info("END   run {},USING {} MS...", new Object[] { name, lastSpan });
        } else {
            log.error("END   run {},USING {} MS...", new Object[] { name, lastSpan, lastThrowable });
        }
    }

    /**
     * ����ǰ��¼��־��Ϣ
     */
    public void beforeProcess() {
        log.debug("BEGIN run {}...", name);
    }

    /**
     * ������һ�εĿ�ʼʱ��
     * 
     * @return ���ַ�����ʾ�����һ�ο�ʼʱ��
     */
    public String getLastBeginTimeStr() {
        DateFormat df = DateUtil.DEFAULT_DF_FACOTRY.get();
        return lastBeginTime == null ? "" : df.format(lastBeginTime);
    }

    /**
     * ������һ�εĽ���ʱ��
     * 
     * @return ���ַ�����ʾ�����һ�ν���ʱ��
     */
    public String getLastEndTimeStr() {
        DateFormat df = DateUtil.DEFAULT_DF_FACOTRY.get();
        return lastEndTime == null ? "" : df.format(lastEndTime);
    }

    /**
     * ������һ��ִ�к�Ķ�ջ��Ϣ
     * 
     * @return ���ַ�����ʾ�Ķ�ջ��Ϣ
     */
    public String getLastThrowableStr() {
        return lastThrowable == null ? "" : StringHelper.printThrowable(lastThrowable).toString();
    }

    /**
     * �����չ��Ϣ
     * 
     * @return
     */
    public String getExtendInfo() {
        return null;
    }

    /**
     * ����run()ִ�е��߼����̳�BaseRunnable�ķǳ�������Ҫ��д�˷���
     * 
     * @return ִ�е�����Ϣ
     * @throws Throwable
     */
    public abstract void process() throws Throwable;

    /**
     * run��ʵ��
     */
    @Override
    public synchronized void run() {
        Date begin = new Date();
        Throwable ex = null;
        try {
            beforeProcess();
            process();
        } catch (Throwable e) {
            ex = e;
        }
        Date end = new Date();

        lastBeginTime = begin;
        lastEndTime = end;
        lastSpan = end.getTime() - begin.getTime();
        lastThrowable = ex;
        try {
            afterProcess();
        } catch (Throwable e) {
            log.error("", e);
        }
    }

    /**
     * ��BaseRunable���������ת��Ϊ�ַ���ʱ���õķ��� ����BaseRunable��������ɵ��ַ���
     */
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        tmp.append(name);
        tmp.append(":lastBeginTime=");
        tmp.append(getLastBeginTimeStr());
        tmp.append(",lastSpan=");
        tmp.append(lastSpan);
        String ext = getExtendInfo();
        if (StringTools.isNotEmpty(ext)) {
            tmp.append(",");
            tmp.append(ext);
        }
        if (lastThrowable != null) {
            tmp.append("\n");
            StringHelper.printThrowable(tmp, lastThrowable);
        }
        return tmp.toString();
    }
}
