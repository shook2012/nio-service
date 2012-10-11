package com.xunlei.netty.httpserver.handler;

import java.util.List;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.stereotype.Service;
import com.xunlei.httptool.util.JsonObjectUtil;
import com.xunlei.httptool.util.RtnConstants;
import com.xunlei.httptool.util.RtnError;
import com.xunlei.netty.httpserver.component.XLContextAttachment;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import com.xunlei.netty.httpserver.exception.AbstractHttpServerError;
import com.xunlei.netty.httpserver.exception.IllegalParameterError;
import com.xunlei.util.StringHelper;

/**
 * @author ZengDong
 * @since 2011-3-18 ����06:41:40
 */
@Service
public class PlainHandler extends TextResponseHandler {

    public void appendDebugInfo(XLContextAttachment attach, StringBuilder content) {
        if (serverConfig.isDebugEnable()) {
            XLHttpRequest request = attach.getRequest();
            if (null != request.getParameter("debug", null)) {
                content.append("\n\n\n---DEBUG---------------------------------------------\n");
                content.append(attach.getName()).append("\n");
                content.append(attach.getTimeSpanInfo()).append("\n\n");

                List<Throwable> ts = attach.getThrowables();
                if (null != ts) {
                    for (Throwable ex : ts) {
                        StringHelper.printThrowable(content, ex).append("\n\n");
                    }
                }
            }
        }
    }

    public StringBuilder _buildContentString(XLContextAttachment attach, Object cmdReturnObj) {
        StringBuilder content = new StringBuilder();
        content.append(cmdReturnObj == null ? responseReturnNullInfo : cmdReturnObj);
        appendDebugInfo(attach, content);
        return content;
    }

    @Override
    public String buildContentString(XLContextAttachment attach, Object cmdReturnObj) {
        XLHttpResponse response = attach.getResponse();
        // ContentType type = response.getInnerContentType();
        // ��ǰ������һ���Ƿ��ڴ����������һ��,�������ﴦ�������������
        // if (!type.equals(ContentType.plain))
        // return null;
        response.setHeaderIfEmpty(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=" + response.getContentCharset());
        StringBuilder content = _buildContentString(attach, cmdReturnObj);
        return content.toString();
    }

    @Override
    public Object handleThrowable(XLContextAttachment attach, Throwable ex) throws Exception {
        // ��ǰ������һ���Ƿ��ڴ����������һ��,�������ﴦ�������������
        // if (!type.equals(ContentType.plain))
        // return null;
        XLHttpResponse response = attach.getResponse();
        XLHttpRequest request = attach.getRequest();
        if (ex instanceof RtnError) {
            return ((RtnError) ex).getJson();
        }
        if (ex instanceof IllegalParameterError) {
            return JsonObjectUtil.getRtnAndDataJsonObject(RtnConstants.PARAM_ILLEGAL, JsonObjectUtil.buildMap("msg", ex.getMessage()));
        }
        if (ex instanceof AbstractHttpServerError) {
            HttpResponseStatus status = ((AbstractHttpServerError) ex).getStatus();
            response.setStatus(status);
            return ex.getMessage();// �ڲ�����ֻ�ô����message
        }

        logThrowable(attach, request, response, ex);
        return StringHelper.printThrowableSimple(ex); // ���������ӡ����Ϣ
    }
}
