package com.xunlei.netty.httpserver.handler;

import java.lang.reflect.InvocationTargetException;
import javax.annotation.Resource;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xunlei.netty.httpserver.async.AsyncProxyHandler;
import com.xunlei.netty.httpserver.component.XLContextAttachment;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import com.xunlei.netty.httpserver.exception.ProcessFinishedError;
import com.xunlei.netty.httpserver.exception.ProcessTimeoutError;
import com.xunlei.netty.httpserver.exception.ResourceNotFoundError;
import com.xunlei.netty.httpserver.util.HttpServerConfig;
import com.xunlei.spring.AfterConfig;

/**
 * @author ZengDong
 * @since 2010-3-25 ����01:12:16
 */
@Service
public class TextResponseHandlerManager extends HandlerManager<TextResponseHandler> {

    @Autowired
    private HttpServerConfig serverConfig;
    @Resource
    private PlainHandler plainHandler;
    @Resource
    private PlainHandlerExt plainHandlerExt;

    @AfterConfig
    public void initHandlerChain() {// Ĭ�ϼ���plain��html������
        addFirst(plainHandlerExt);
        addLast(plainHandler);
    }

    private void setContent(XLContextAttachment attach, Object cmdReturnObj) {
        XLHttpResponse response = attach.getResponse();
        if (!response.isContentSetted()) {
            String contentStr = null;

            // ���������Ѿ�ע��õ� �ı����ݴ�����
            for (TextResponseHandler trh : getHandlerChain()) {
                contentStr = trh.buildContentString(attach, cmdReturnObj);// ����з�������,������resp��content,���˳�
                if (contentStr != null) {
                    response.setContentString(contentStr);
                    return;
                }
            }
        }
    }

    // public void writeTimeoutResponse(Channel channel) {
    // XLHttpResponse response = new XLHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SERVICE_UNAVAILABLE);
    // Checkers.checkChannelOpen(channel);
    // response.setContent(ChannelBuffers.copiedBuffer("process timeout", CharsetUtil.UTF_8));
    // boolean close = setHeader(null, response);
    //
    // // statistics.writeBegin(attach); // TODO:��ͳ�Ʊ�����ʱ
    // ChannelFuture future = channel.write(response);
    // if (close) {
    // future.addListener(ChannelFutureListener.CLOSE);
    // }
    // }

    public void writeResponse(XLContextAttachment attach, Object cmdReturnObj) {
        try {
            Channel channel = attach.getChannelHandlerContext().getChannel();
            attach.checkChannelOrThread();
            XLHttpResponse response = attach.getResponse();
            XLHttpRequest request = attach.getRequest();

            serverConfig.getStatistics().writeBegin(attach);
            setContent(attach, cmdReturnObj);
            response.packagingCookies();
            attach.setKeepAliveHeader();

            // Write the response.
            // Checkers.checkChannelOpen(channel);
            ChannelFuture future = channel.write(response);
            // Close the connection after the write operation is done if necessary.
            future.addListener(attach);

            // TODO:��ʱֻ�� ��������Ӧʱ,��д��accessLog��
            // TODO:�������Ӧ����ʱ����,�Ƿ�д��accessLog����
            serverConfig.getAccessLog().log(request, response);
        } finally {
            // attach.close(); //bug ���async��channel����
            // TODO:�������� ��дrespʱ�Զ��ر�coloseable,�����������closeableΪ���ࣺ
            // һ���� ���������رյģ���ҵ���������Ҫ�رյ�Io
            // ��һ���� ���쳣����ʱ����Ҫ�رյģ����첽client Proxy ����� channel���������������� ���ӳ������Ӧ���ǲ�close�ģ�ֻ���쳣ʱclose
        }
    }

    // private static final Logger log = Log.getLogger();

    /**
     * ����ҵ���д������
     */
    public Object handleThrowable(XLContextAttachment attach, Throwable e) throws Exception {
        Object cmdReturnObj = null;
        Throwable ex = e;
        // 1.��ȷ����ʵ���쳣
        if (ex instanceof NoSuchMethodException || ex instanceof SecurityException) {
            ex = ResourceNotFoundError.INSTANCE;
        } else {
            if (ex instanceof InvocationTargetException) {
                ex = ((InvocationTargetException) ex).getTargetException();
                // ���쳣�����������Ѿ������ˣ��������ʣ��Ĳ�������дresponse�Ȳ������׳����쳣ǰ�Ĵ��붼�д�������Ҫ�ٵ���dispatch��finally���֣�������������쳣
                if (ex instanceof ProcessFinishedError) {
                    return AsyncProxyHandler.ASYNC_RESPONSE;
                }
            } else if (ex instanceof InterruptedException) {
                ex = ProcessTimeoutError.INSTANCE;
            }
        }
        attach.registerThrowable(ex);// ��¼�� attach��
        // 2.ʹ���쳣������������
        for (TextResponseHandler th : getHandlerChain()) {
            cmdReturnObj = th.handleThrowable(attach, ex);
            if (cmdReturnObj != null) {
                return cmdReturnObj;
            }
        }
        return cmdReturnObj;
    }
}
