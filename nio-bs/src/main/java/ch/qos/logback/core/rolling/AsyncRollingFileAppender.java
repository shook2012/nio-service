package ch.qos.logback.core.rolling;

import com.xunlei.netty.httpserver.component.SystemChecker;

/**
 * 在httpserver进行高并发访问情况下，为了不上日志写文件时因为同步问题而让pipiline线程池堵塞
 * 
 * @author ZengDong
 * @param <E>
 * @since 2011-12-16 上午11:17:32
 */
public class AsyncRollingFileAppender<E> extends RollingFileAppender<E> {

    @Override
    protected void subAppend(final E event) {
        // perform actual sending asynchronously
        // 同SMTPAppenderBase一样，都走后台异步
        // 2012-9-20 增加防止爆内存的检查，日志过多会导致IO过高和内存溢出，如果过多就临时关闭日志功能以保护系统
        if (SystemChecker.isLogEnabled()) {
            this.context.getExecutorService().execute(new Runnable() {

                @Override
                public void run() {
                    AsyncRollingFileAppender.super.subAppend(event);
                }
            });
        }
    }
}
