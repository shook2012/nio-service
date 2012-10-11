package com.xunlei.netty.httpserver.cmd;

import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import com.xunlei.netty.httpserver.component.XLHttpResponse.ContentType;
import com.xunlei.netty.httpserver.util.AntiDos;
import com.xunlei.netty.httpserver.util.IPAuthenticator;
import com.xunlei.spring.Config;
import com.xunlei.util.DateStringUtil;
import com.xunlei.util.StringTools;
import com.xunlei.util.concurrent.ConcurrentUtil;

/**
 * @author ZengDong
 * @since 2010-9-13 ����01:24:02
 */
public abstract class BaseStatCmd extends BaseCmd {

    private AntiDos baseStatCmdGlobalAntiDos = null; // һ�� ����һ�� antiDos
    @Config(resetable = true)
    private String antiDos4BaseStatCmdGlobal;

    /**
     * ��ʼ����������Ӧ������ plain,У��ip,У���Ƿ�Ƶ������
     */
    protected void init(XLHttpRequest request, XLHttpResponse response) {
        response.setInnerContentType(ContentType.plain);
        IPAuthenticator.auth(this, request);
        if (baseStatCmdGlobalAntiDos != null) {
            baseStatCmdGlobalAntiDos.visitAndCheck(request.getRemoteIP());
        }
    }

    /**
     * ��ʼ��,�����ش��е�ǰʱ����� StringBuilder
     */
    protected StringBuilder initWithTime(XLHttpRequest request, XLHttpResponse response) {
        init(request, response);
        return new StringBuilder().append(DateStringUtil.DEFAULT_DATE_STRING_UTIL.now()).append('\n');
    }

    public AntiDos getBaseStatCmdGlobalAntiDos() {
        return baseStatCmdGlobalAntiDos;
    }

    public void setAntiDos4BaseStatCmdGlobal(String antiDos4BaseStatCmdGlobal) {
        this.antiDos4BaseStatCmdGlobal = antiDos4BaseStatCmdGlobal;
        if (StringTools.isEmpty(antiDos4BaseStatCmdGlobal)) {
            baseStatCmdGlobalAntiDos = null;
        } else {
            baseStatCmdGlobalAntiDos = new AntiDos(this.antiDos4BaseStatCmdGlobal).initSweeper(0, ConcurrentUtil.getDaemonExecutor());
        }
    }
}
