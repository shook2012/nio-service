package com.xunlei.netty.httpserver.handler;

import java.util.Collections;
import java.util.Set;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import com.xunlei.netty.httpserver.component.XLContextAttachment;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import com.xunlei.netty.httpserver.util.HttpServerConfig;
import com.xunlei.spring.Config;
import com.xunlei.util.Log;

/**
 * @author ZengDong
 * @since 2011-3-18 ����06:19:53
 */
public abstract class TextResponseHandler implements Handler {

    public static final Logger log = Log.getLogger();
    @Autowired
    protected HttpServerConfig serverConfig;
    @Config
    protected String responseReturnNullInfo = "cmd return null";
    @Autowired
    protected TextResponseHandlerManager textResponseHandlerManager;
    /** ��������־���쳣�б� */
    @Config(resetable = true, split = ",")
    protected Set<String> logThrowableIgnoreList = Collections.emptySet();

    /**
     * ����ǰ��attach�е�response�������
     * 
     * @param attach
     * @param cmdReturnObj
     * @return ��null ��ʾ�������,null ��ʾ������
     */
    public abstract String buildContentString(XLContextAttachment attach, Object cmdReturnObj);

    public abstract Object handleThrowable(XLContextAttachment attach, Throwable ex) throws Exception;

    public static void logError(final String mailTitleInfo, final String info, final Object... args) {
        // ConcurrentUtil.getDefaultExecutor().execute(new Runnable() {// ��Ϊ�°汾1.0.0��logback���ʼ�ʱ���Զ����̳߳��ϲ��������Բ����ⲿ������
        //
        // @Override
        // public void run() {
        Object objEx = args[args.length - 1];
        String exInfo = "";
        if (objEx instanceof Throwable) {
            exInfo = ((Throwable) objEx).getClass().getSimpleName();
        }
        MDC.put("mailTitle", exInfo + ": " + mailTitleInfo);
        log.error(info, args);
        // }
        // });
    }

    public void logThrowable(final XLContextAttachment attach, final XLHttpRequest request, XLHttpResponse response, final Throwable ex) {
        response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR); // �����δ֪�����򷵻�500
        if (logThrowableIgnoreList.contains(ex.getClass().getName())) { // ������������豨����쳣���򲻽��뷢�ʼ�����
            return;
        }
        logError(request.getUrl(), "\n{}", request.getDetailInfo(), ex);
    }
}
