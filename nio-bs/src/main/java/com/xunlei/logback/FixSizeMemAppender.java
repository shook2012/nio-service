package com.xunlei.logback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * ��־���������Խ����е���־�¼�������
 * 
 * @author ZengDong
 * @since 2010-9-20 ����09:12:01
 */
public class FixSizeMemAppender<E> extends UnsynchronizedAppenderBase<E> {

    /**
     * �̶���С����־������
     * 
     * @author ZengDong
     */
    public static class FixSizeLog {

        /**
         * ��־��������Ӧ������
         */
        private String loggerName;
        /**
         * ������־������
         */
        private String[] bufferedLog;
        /**
         * ��ǰ�����е���־����
         */
        private AtomicInteger index = new AtomicInteger(0);
        /**
         * ��־��������С
         */
        private int size;

        /**
         * ����̶���С����־��¼��
         * 
         * @param loggerName ��־��Ӧ�����֣�һ��Ϊ����
         * @param size ��־�������Ĵ�С
         */
        public FixSizeLog(String loggerName, int size) {
            this.loggerName = loggerName;
            this.size = size;
            this.bufferedLog = new String[size];
        }

        /**
         * ��¼��־��Ϣmsg
         * 
         * @param msg ��־��Ϣ
         */
        public void log(String msg) {
            bufferedLog[index.getAndIncrement() % size] = msg;
        }

        /**
         * ������е���־��¼
         */
        @Override
        public String toString() {
            return sub(0, size);
        }

        public String tail(int num) {
            int end = index.get();
            int begin = end - num;
            if (begin < 0) {
                begin = 0;
            }
            return sub(begin, end);
        }

        public String sub(int begin, int end) {
            if (begin < 0 || end > size) {
                throw new IndexOutOfBoundsException();
            }
            StringBuilder sb = new StringBuilder();
            int offset = index.get();// TODO:���������ӡ�ٶȻ�û�� log�ٶȿ죬��������
            if (offset <= size) {// ˵��û������
                end = Math.min(end, offset);
                offset = 0;
            }

            for (int i = begin; i < end; i++) {
                int idx = (i + offset) % size;
                String item = bufferedLog[idx];
                if (item != null) {
                    sb.append(item);
                }
            }
            return sb.toString();
        }

        /**
         * �����־��������Ӧ�����֣�һ��Ϊ����
         * 
         * @return
         */
        public String getLoggerName() {
            return loggerName;
        }
    }

    /**
     * ��־����
     */
    public static final Map<String, FixSizeLog> FIX_SIZE_LOG_MAP = new HashMap<String, FixSizeLog>();

    /**
     * ���loggerName��Ӧ����־������
     * 
     * @param loggerName
     * @return
     */
    public static FixSizeLog getFixSizeLog(String loggerName) {
        return FIX_SIZE_LOG_MAP.get(loggerName);
    }

    /**
     * ���log��Ӧ����־������
     * 
     * @param log
     * @return
     */
    public static FixSizeLog getFixSizeLog(Logger log) {
        return FIX_SIZE_LOG_MAP.get(log.getName());
    }

    /**
     * ��־�¼�ת����������־�¼�ת�����ַ���
     */
    protected Layout<E> layout;
    /**
     * ��־������ÿһ����־�������Ĵ�С
     */
    protected int size = 100;

    /**
     * ����һ����־�¼�
     */
    @Override
    protected void append(E e) {
        if (e instanceof LoggingEvent) {
            LoggingEvent le = (LoggingEvent) e;
            String loggerName = le.getLoggerName();
            FixSizeLog fsl = FIX_SIZE_LOG_MAP.get(loggerName);
            if (fsl == null) {
                synchronized (this) {// ��ͬ��
                    fsl = new FixSizeLog(loggerName, size);
                    FIX_SIZE_LOG_MAP.put(loggerName, fsl);
                }
            }
            fsl.log(layout.doLayout(e));
        }
    }

    /**
     * �����־�¼�ת����
     * 
     * @return
     */
    public Layout<E> getLayout() {
        return layout;
    }

    /**
     * �����־������Ĭ�ϵ���־��������С
     */
    public int getSize() {
        return size;
    }

    /**
     * �����ռ��¼�ת����
     * 
     * @param layout
     */
    public void setLayout(Layout<E> layout) {
        this.layout = layout;
    }

    /**
     * ������־������Ĭ�ϵ���־��������С
     * 
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }
}
