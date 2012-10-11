package ch.qos.logback.core.rolling;

import com.xunlei.netty.httpserver.component.SystemChecker;

/**
 * ��httpserver���и߲�����������£�Ϊ�˲�����־д�ļ�ʱ��Ϊͬ���������pipiline�̳߳ض���
 * 
 * @author ZengDong
 * @param <E>
 * @since 2011-12-16 ����11:17:32
 */
public class AsyncRollingFileAppender<E> extends RollingFileAppender<E> {

    @Override
    protected void subAppend(final E event) {
        // perform actual sending asynchronously
        // ͬSMTPAppenderBaseһ�������ߺ�̨�첽
        // 2012-9-20 ���ӷ�ֹ���ڴ�ļ�飬��־����ᵼ��IO���ߺ��ڴ����������������ʱ�ر���־�����Ա���ϵͳ
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
