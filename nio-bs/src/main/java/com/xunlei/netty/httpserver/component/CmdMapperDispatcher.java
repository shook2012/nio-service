package com.xunlei.netty.httpserver.component;

import java.lang.reflect.Method;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xunlei.netty.httpserver.async.AsyncProxyHandler;
import com.xunlei.netty.httpserver.cmd.BaseCmd;
import com.xunlei.netty.httpserver.cmd.CmdMappers.CmdMeta;
import com.xunlei.netty.httpserver.component.XLHttpResponse.ContentType;
import com.xunlei.netty.httpserver.exception.ResourceNotFoundError;
import com.xunlei.netty.httpserver.handler.TextResponseHandlerManager;

/**
 * ����ӳ��ַ���
 * 
 * <pre>
 * Լ���� http://host:port /cmdname [/methodname] [?callback=value&debug=1]
 * ��cmdname�ҵ�ָ����cmd��(����Ĭ����CmdΪ��׺)
 * ��ָ����methodnameʱ,����cmd��Ķ�Ӧ���Ƶķ���,δָ��ʱ����process����
 * </pre>
 * 
 * <pre>
 * ͨ��CmdMapper��ӳ���ϵ���ҵ���Ӧ����
 * ��httpserver����ʱ,dispatcher���ʼ��cmdMappers�������������
 * </pre>
 * 
 * @author ZengDong
 * @since 2010-6-8 ����01:53:26
 */
@Service
public class CmdMapperDispatcher extends BasePageDispatcher {

    @Autowired
    protected TextResponseHandlerManager handlerManager;

    // private static final Logger log = Log.getLogger();

    protected Object _dispatch(XLContextAttachment attach) throws Exception {
        XLHttpRequest request = attach.getRequest();
        XLHttpResponse response = attach.getResponse();
        String path = request.getPath();

        CmdMeta meta = cmdMappers.getCmdMeta(path);
        if (meta == null) {
            throw ResourceNotFoundError.INSTANCE;
        }
        // ��attach��������CmdMeta
        attach.setCmdMeta(meta);

        if (meta.isDisable()) {
            throw ResourceNotFoundError.INSTANCE;
        }

        BaseCmd cmd = meta.getCmd();
        Method method = meta.getMethod();
        try {
            attach.registerProcessThread();
            // 2011-01-21 ��ͨ��������������Щ�ӿ�
            // if (method == null) {
            // return cmd.process(request, response);
            // } else {
            return method.invoke(cmd, request, response);
            // }
        } finally {
            attach.unregisterProcessThread();
        }
    }

    @Override
    protected void dispatch(XLContextAttachment attach) throws Exception {
        attach.checkChannelOrThread();
        Object cmdReturnObj = null;
        try {
            if (SystemChecker.isDenialOfService()) {
                XLHttpResponse resp = attach.getResponse();
                resp.setStatus(HttpResponseStatus.SERVICE_UNAVAILABLE); // �ܾ����� 503
                resp.setInnerContentType(ContentType.plain);
                cmdReturnObj = "Denial Of Service";
            } else {
                cmdReturnObj = _dispatch(attach);
            }
        } catch (Throwable ex) {
            cmdReturnObj = handlerManager.handleThrowable(attach, ex);
        } finally {
            if (cmdReturnObj != AsyncProxyHandler.ASYNC_RESPONSE) {
                handlerManager.writeResponse(attach, cmdReturnObj);
            }
        }
    }

    @Override
    public void init() {
        // ���ȼ���͵���
        // long before = System.currentTimeMillis();
        cmdMappers.initAutoMap();
        cmdMappers.initCmdMapperDefinedMap();
        cmdMappers.initConfigMap();
        cmdMappers.printFuzzyMap();
        cmdMappers.resetCmdConfig();
        // log.debug("CmdMapperDispatcher init USING {} MS",System.currentTimeMillis() - before);//���Է��ֳ�ʼ������ʱ
    }
}
