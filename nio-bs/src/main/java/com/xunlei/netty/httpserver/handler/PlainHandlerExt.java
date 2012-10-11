package com.xunlei.netty.httpserver.handler;

import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xunlei.netty.httpserver.component.XLContextAttachment;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import com.xunlei.netty.httpserver.component.XLHttpResponse.ContentType;

/**
 * @author ZengDong
 * @since 2011-3-18 ����06:19:53
 */
@Service
public class PlainHandlerExt extends TextResponseHandler {

    @Autowired
    private PlainHandler plainHandler;// ��ǰHtmlHandlerҵ������� plainһ��

    @Override
    public String buildContentString(XLContextAttachment attach, Object cmdReturnObj) {
        XLHttpResponse response = attach.getResponse();
        ContentType type = response.getInnerContentType();
        if (type.equals(ContentType.html)) {
            response.setHeaderIfEmpty(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=" + response.getContentCharset());
        } else if (type.equals(ContentType.xml)) {
            response.setHeaderIfEmpty(HttpHeaders.Names.CONTENT_TYPE, "text/xml; charset=" + response.getContentCharset());
        } else {
            return null;
        }

        return plainHandler._buildContentString(attach, cmdReturnObj).toString();
    }

    @Override
    public Object handleThrowable(XLContextAttachment attach, Throwable ex) throws Exception {
        return null;
        // ֱ��ʹ��Ĭ�ϵ�plainHandler�����������,����return null������ע�͵�Ч����һ����

        // XLHttpResponse response = attach.getResponse();
        // ContentType type = response.getInnerContentType();
        // if (type.equals(ContentType.html) || type.equals(ContentType.xml)) {
        // return plainHandler.handleThrowable(attach, ex);
        // } else {
        // return null;
        // }
    }

}
