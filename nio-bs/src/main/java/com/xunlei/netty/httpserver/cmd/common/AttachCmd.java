package com.xunlei.netty.httpserver.cmd.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xunlei.netty.httpserver.cmd.BaseStatCmd;
import com.xunlei.netty.httpserver.cmd.annotation.Cmd;
import com.xunlei.netty.httpserver.cmd.annotation.CmdAuthor;
import com.xunlei.netty.httpserver.cmd.annotation.CmdCategory;
import com.xunlei.netty.httpserver.cmd.annotation.CmdContentType;
import com.xunlei.netty.httpserver.cmd.annotation.CmdParam;
import com.xunlei.netty.httpserver.cmd.annotation.CmdParams;
import com.xunlei.netty.httpserver.cmd.annotation.CmdSession;
import com.xunlei.netty.httpserver.component.TimeoutInterrupter;
import com.xunlei.netty.httpserver.component.XLContextAttachment;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import com.xunlei.netty.httpserver.component.XLHttpResponse.ContentType;
import com.xunlei.netty.httpserver.util.CmdSessionType;
import com.xunlei.util.StringHelper;

@Service
@CmdCategory("system")
public class AttachCmd extends BaseStatCmd {

    @Autowired
    private TimeoutInterrupter timeoutInterrupter;

    // @formatter:off
    @Cmd("获取当前服务器运行中的attach列表")
    @CmdAuthor("zengdong")
    @CmdSession(type = CmdSessionType.NOT_COMPELLED)
    @CmdParams({ @CmdParam(name = "userId", desc = "需要查看的用户的userId", type = Long.class), })
    @CmdContentType({ ContentType.plain })
    // @formatter:on
    public Object process(XLHttpRequest request, XLHttpResponse response) throws Exception {
        StringBuilder tmp = initWithTime(request, response);
        long now = System.currentTimeMillis();
        List<XLContextAttachment> attachs = new ArrayList<XLContextAttachment>(timeoutInterrupter.getLiveAttach());
        Collections.sort(attachs);
        tmp.append("liveAttachs:[").append(attachs.size()).append("]\n");
        for (XLContextAttachment a : attachs) {
            tmp.append(StringHelper.printLine(100, '-'));
            a.getDetailInfo(tmp, now);
        }
        return tmp;
    }
}
