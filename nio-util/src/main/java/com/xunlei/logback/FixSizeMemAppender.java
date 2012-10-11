package com.xunlei.logback;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * 日志容器，可以将所有的日志事件做缓存
 * 
 * @author ZengDong
 * @since 2010-9-20 下午09:12:01
 */
public class FixSizeMemAppender<E> extends UnsynchronizedAppenderBase<E> {

    /**
     * 固定大小的日志缓存器
     * 
     * @author ZengDong
     */
    public static class FixSizeLog {

        /**
         * 日志缓存器对应的名字
         */
        private String loggerName;
        /**
         * 缓存日志的数组
         */
        private String[] bufferedLog;
        /**
         * 当前缓存中的日志数量
         */
        private AtomicInteger index = new AtomicInteger(0);
        /**
         * 日志缓存区大小
         */
        private int size;

        /**
         * 构造固定大小的日志记录器
         * 
         * @param loggerName 日志对应的名字，一般为类名
         * @param size 日志缓存区的大小
         */
        public FixSizeLog(String loggerName, int size) {
            this.loggerName = loggerName;
            this.size = size;
            this.bufferedLog = new String[size];
        }

        /**
         * 记录日志信息msg
         * 
         * @param msg 日志信息
         */
        public void log(String msg) {
            bufferedLog[index.getAndIncrement() % size] = msg;
        }

        /**
         * 获得所有的日志记录
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
            int offset = index.get();// TODO:这里如果打印速度还没有 log速度快，会有问题
            if (offset <= size) {// 说明没有填满
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
         * 获得日志缓存器对应的名字，一般为类名
         * 
         * @return
         */
        public String getLoggerName() {
            return loggerName;
        }
    }

    /**
     * 日志容器
     */
    public static final Map<String, FixSizeLog> FIX_SIZE_LOG_MAP = new HashMap<String, FixSizeLog>();

    /**
     * 获得loggerName对应的日志缓存器
     * 
     * @param loggerName
     * @return
     */
    public static FixSizeLog getFixSizeLog(String loggerName) {
        return FIX_SIZE_LOG_MAP.get(loggerName);
    }

    /**
     * 获得log对应的日志缓存器
     * 
     * @param log
     * @return
     */
    public static FixSizeLog getFixSizeLog(Logger log) {
        return FIX_SIZE_LOG_MAP.get(log.getName());
    }

    /**
     * 日志事件转换器，将日志事件转换成字符串
     */
    protected Layout<E> layout;
    /**
     * 日志容器中每一个日志缓存器的大小
     */
    protected int size = 100;

    /**
     * 处理一个日志事件
     */
    @Override
    protected void append(E e) {
        if (e instanceof LoggingEvent) {
            LoggingEvent le = (LoggingEvent) e;
            String loggerName = le.getLoggerName();
            FixSizeLog fsl = FIX_SIZE_LOG_MAP.get(loggerName);
            if (fsl == null) {
                synchronized (this) {// 简单同步
                    fsl = new FixSizeLog(loggerName, size);
                    FIX_SIZE_LOG_MAP.put(loggerName, fsl);
                }
            }
            fsl.log(layout.doLayout(e));
        }
    }

    /**
     * 获得日志事件转换器
     * 
     * @return
     */
    public Layout<E> getLayout() {
        return layout;
    }

    /**
     * 获得日志容器中默认的日志缓存器大小
     */
    public int getSize() {
        return size;
    }

    /**
     * 设置日记事件转换器
     * 
     * @param layout
     */
    public void setLayout(Layout<E> layout) {
        this.layout = layout;
    }

    /**
     * 设置日志容器中默认的日志缓存器大小
     * 
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }
}
