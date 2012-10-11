package com.xunlei.netty.httpserver.exception;

import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * �����ж�ҵ�������̣��������Ѿ������ˣ�����Ҫ����ִ��ʣ����룬ֱ�ӷ��ظ��û����ɣ���ǰ�Ĵ����뱣֤�Ѿ�дresponse�ˣ�
 * 
 * @since 2011-11-30
 * @author hujiachao
 */
public class ProcessFinishedError extends AbstractHttpServerError {

    private static final long serialVersionUID = 2083669464202955119L;
    public final static ProcessFinishedError INSTANCE = new ProcessFinishedError();

    private ProcessFinishedError() {
    }

    @Override
    public HttpResponseStatus getStatus() {
        return HttpResponseStatus.OK;
    }
}
