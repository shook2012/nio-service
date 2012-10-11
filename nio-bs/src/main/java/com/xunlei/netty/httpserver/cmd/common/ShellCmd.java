package com.xunlei.netty.httpserver.cmd.common;

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import com.xunlei.command.CommandService;
import com.xunlei.netty.httpserver.cmd.BaseStatCmd;
import com.xunlei.netty.httpserver.cmd.annotation.CmdCategory;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import com.xunlei.netty.httpserver.component.XLHttpResponse.ContentType;
import com.xunlei.spring.AfterConfig;
import com.xunlei.spring.Config;
import com.xunlei.util.Log;
import com.xunlei.util.StringTools;

/**
 * �ѳ��õĵ���ʹ�õ����������ҳ����,ע����Щ�����Ȩ��
 * 
 * @author ZengDong
 * @since 2011-3-15 ����01:48:56
 */
@Service
@CmdCategory("system")
public class ShellCmd extends BaseStatCmd {

    private static final Logger log = Log.getLogger();
    @Config(split = "\\|\\|", resetable = true)
    private String[] shells = {};// ����
    private String[] shellsReal = {};// ʵ�ʵ�����
    @Config(resetable = true)
    private String shellsDefaultCharset = "GBK";

    @AfterConfig
    public void init() {
        String name = ManagementFactory.getRuntimeMXBean().getName();// ����:3948@twin0942
        String selfPid = StringTools.splitAndTrim(name, "@").get(0);
        shellsReal = new String[shells.length];
        for (int i = 0; i < shells.length; i++) {
            shellsReal[i] = shells[i].replaceAll("#SELF_PID#", selfPid);
        }
        if (shellsReal != null && shellsReal.length > 0) {
            log.warn("INIT SHELLS:\t\t{}", Arrays.toString(shellsReal));
        }
    }

    public Object process(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        int index = request.getParameterInteger("exeIndex", -1);
        if (index == -1) {// ��ʾ��ҳ��
            response.setInnerContentType(ContentType.html);
            StringBuilder tmp = new StringBuilder();
            tmp.append("<html><body><table><tbody>\n");
            for (int i = 0; i < shellsReal.length; i++) {
                String m = shellsReal[i];
                tmp.append(String.format("<tr><td><a target=\"_blank\" href=\"?exeIndex=%s\">%s</a></td></tr>\n", i, m));
            }
            tmp.append("</tbody></table></body></html>");
            return tmp.toString();
        }
        CommandService cs = new CommandService(shellsReal[index]);
        cs.execute(shellsDefaultCharset);
        return cs.getProcessingDetail();
    }
}
