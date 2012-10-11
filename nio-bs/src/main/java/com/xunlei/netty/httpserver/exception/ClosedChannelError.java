package com.xunlei.netty.httpserver.exception;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author ZengDong
 * @since 2010-11-3 ����02:20:33
 */
public class ClosedChannelError extends AbstractHttpServerError {

    private static final long serialVersionUID = 1L;
    public final static ClosedChannelError INSTANCE = new ClosedChannelError();

    private ClosedChannelError() {
    }

    @Override
    public HttpResponseStatus getStatus() {
        throw this;// ֱ���׳�,��BasePageDispatcher�� exceptionCaught��ͳһ����־
    }

}
