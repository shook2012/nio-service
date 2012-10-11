package com.xunlei.netty.httpserver.cmd.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.net.InetAddressCachePolicy;
import com.xunlei.netty.httpserver.HttpServerPipelineFactory;
import com.xunlei.netty.httpserver.cmd.BaseStatCmd;
import com.xunlei.netty.httpserver.cmd.CmdMapper;
import com.xunlei.netty.httpserver.cmd.CmdMappers;
import com.xunlei.netty.httpserver.cmd.CmdMappers.CmdMeta;
import com.xunlei.netty.httpserver.cmd.CmdOverride;
import com.xunlei.netty.httpserver.cmd.annotation.CmdCategory;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import com.xunlei.netty.httpserver.component.XLHttpResponse.ContentType;
import com.xunlei.netty.httpserver.util.HttpServerConfig;
import com.xunlei.netty.httpserver.util.IPAuthenticator;
import com.xunlei.spring.ConfigAnnotationBeanPostProcessor;
import com.xunlei.util.HttpUtil;
import com.xunlei.util.InetAddressCacheUtil;
import com.xunlei.util.Log;
import com.xunlei.util.NumberStringUtil;
import com.xunlei.util.StringTools;

/**
 * ʵʱ����
 * 
 * @author ZengDong
 * @since 2010-5-23 ����12:15:48
 */
@Service
@CmdCategory("system")
public class SettingCmd extends BaseStatCmd {

    private static final Logger log = Log.getLogger();
    @Autowired
    private CmdMappers cmdMappers;
    @Autowired
    private HttpServerConfig config;
    @Autowired
    private ConfigAnnotationBeanPostProcessor configProcessor;
    @Resource
    private HttpServerPipelineFactory httpServerPipelineFactory;
//    @Autowired
//    private StatCmd statCmd;

    /**
     * <pre>
     * �����������нӿڶ�����������¼
     * �������http://www.baidu.com/search/robots.html
     * </pre>
     */
    @CmdMapper("/robots.txt")
    @CmdOverride
    public Object robots(XLHttpRequest request, XLHttpResponse response) throws Exception {
        response.setInnerContentType(ContentType.plain);
        return "User-agent: *\nDisallow: /\n";
    }

    private String printUrlList(List<String> url, String tips) {
        StringBuilder sb = new StringBuilder();
        for (String u : url) {
            sb.append(String.format("<a title=\"%s\" href=\"%s\" target=\"_blank\">%s</a>&nbsp", tips, u, u));
        }
        return sb.toString();
    }

    /**
     * ��ʾ��ǰ�Ѿ�ӳ�������
     */
    public Object cmds(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        boolean useTxt = request.getParameterBoolean("txt", false);
        if (!useTxt)
            response.setInnerContentType(ContentType.html);

        StringBuilder tmp = new StringBuilder();

        Map<CmdMeta, List<String>> cmd_urls_map = cmdMappers.getReverseCmdAllSortedMap();
        Collection<Map.Entry<CmdMeta, List<String>>> entry = cmd_urls_map.entrySet();
        boolean order = request.getParameterBoolean("order", false);
        if (order) {
            ArrayList<Map.Entry<CmdMeta, List<String>>> sorting = new ArrayList<Map.Entry<CmdMeta, List<String>>>(entry);
            Collections.sort(sorting, new Comparator<Map.Entry<CmdMeta, List<String>>>() {

                public int compare(Map.Entry<CmdMeta, List<String>> o1, Map.Entry<CmdMeta, List<String>> o2) {
                    return (int) (o2.getKey().getStat().getAllNum() - o1.getKey().getStat().getAllNum());
                }
            });
            entry = sorting;
        }

        long allNum = 0;
        for (Entry<CmdMeta, List<String>> e : cmd_urls_map.entrySet()) {
            allNum += e.getKey().getStat().getAllNum(); // 2012-03-27 �ϵķ�ʽ��long allNum = statCmd.getProcessTSS().getAllNum() + 1;// +1����Ϊ�߳��ܵ�����ʱallNum���cmdNum��һ������
        }

        if (useTxt) {
            boolean printHead = true;
            String format = "%-40s %s\n";
            for (Entry<CmdMeta, List<String>> e : entry) {
                CmdMeta meta = e.getKey();

                List<String> url = e.getValue();
                if (printHead) {
                    tmp.append(meta.getStat().getTableHeader());
                    printHead = false;
                }
                long cmdNum = meta.getStat().getAllNum();
                String percentageStr = allNum == 0 || cmdNum == 0 ? "" : NumberStringUtil.DEFAULT_PERCENT.formatByDivide(cmdNum, allNum);
                tmp.append(String.format(format, meta, url));
                tmp.append(meta.getStat().toString(percentageStr));
                tmp.append("\n");
            }
        } else {
            String timeStatFmt = "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";
            tmp.append("<html><body><table width=\"100%\"><tbody>\n");
            tmp.append(String.format(timeStatFmt, "", "<a href=\"?order=" + (order ? 0 : 1) + "\">times</a>", "avg", "slow", "slow_avg", "max", "slow_span", "all_span"));
            for (Entry<CmdMeta, List<String>> e : entry) {
                CmdMeta meta = e.getKey();
                List<String> url = e.getValue();

                long cmdNum = meta.getStat().getAllNum();
                String percentageStr = allNum == 0 || cmdNum == 0 ? "" : NumberStringUtil.DEFAULT_PERCENT.formatByDivide(cmdNum, allNum);
                String tips = meta.isDisable() ? "Disable" : meta.getTimeout() == 0 ? "NoTimeout" : meta.getTimeout() + "";

                tmp.append(String.format("<tr><td>%-50s</td><td colspan=\"7\">%s</td></tr>\n", meta, printUrlList(url, tips)));
                tmp.append(meta.getStat().toString(timeStatFmt, percentageStr));
                tmp.append("<tr><td colspan=\"8\">&nbsp;</td></tr>\n");
            }
            tmp.append("</tbody></table></body></html>");
        }

        return tmp.toString();
    }

    /**
     * ��ʾ��ǰconfig
     */
    public Object config(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        return configProcessor.printCurrentConfig(new StringBuilder());
    }

    /**
     * ��ʾ���ε���ʷ��¼
     */
    public Object configHistory(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        StringBuilder tmp = new StringBuilder();
        return tmp.append(configProcessor.getResetHistory());
    }

    /**
     * ��ʾhttpServer�ڲ�����
     */
    public Object httpServerConfig(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        StringBuilder tmp = new StringBuilder();
        for (Field f : config.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()))
                continue;
            f.setAccessible(true);
            try {
                tmp.append(String.format("%-24s%-10s\n", f.getName() + ":", f.get(config)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tmp.append("����IP:\t\t\t").append(HttpUtil.getLocalIP()).append("\n");
        tmp.append("IP������:\t\t").append(IPAuthenticator.getIPAuthenticatorInfo()).append("\n");
        try {
            tmp.append("PIPELINE:\t\t").append(httpServerPipelineFactory.getPipeline()).append("\n");
        } catch (Exception e) {
            log.error("", e);
            tmp.append(e.getMessage()).append("\n");
        }
        return tmp.toString();
    }

    /**
     * �ؼ���config
     */
    public Object reloadConfig(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        StringBuilder tmp = new StringBuilder();
        configProcessor.reloadConfig(tmp);
        return tmp.toString();
    }

    /**
     * �ؼ����������������
     */
    public Object reloadCmdConfig(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        return cmdMappers.resetCmdConfig();
    }

    /**
     * �ؼ���ipfilter
     */
    public Object reloadIpfilter(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        boolean localhostpass = request.getParameterBoolean("localpass", IPAuthenticator.LOCALHOST_PASS);
        log.error("START RELOAD IPFILTER,localpass:{}", localhostpass);
        IPAuthenticator.reload(localhostpass);
        return "reset success";
    }

    /**
     * ��ù���ԱȨ�޼������
     */
    public Object activeIpfilter(XLHttpRequest request, XLHttpResponse response) throws Exception {
        response.setInnerContentType(ContentType.plain);
        int step = request.getParameterInteger("step", -1);
        String key = request.getParameter("key");
        if (step == 1) {
            String mail = request.getParameterCompelled("mail");
            boolean r = IPAuthenticator.sendActiveMail(mail, request.getRemoteIP());
            return "sendActiveMail:" + r;
        } else if (StringTools.isNotEmpty(key)) {
            boolean r = IPAuthenticator.handleActiveMail(request.getParameterCompelled("mail"), request.getRemoteIP(), request.getParameterLong("time"), request.getParameterCompelled("key"));
            return "activeIpfilter:" + r;
        } else {
            Set<String> list = IPAuthenticator.getActiveMailList();
            if (list.isEmpty())
                return "ActiveMailList isEmpty";
            response.setInnerContentType(ContentType.html);
            StringBuilder tmp = new StringBuilder();
            tmp.append("<html><body><table><tbody>\n");
            for (String m : list) {
                tmp.append(String.format("<tr><td><a href=\"?step=1&mail=%s\">%s</a></td></tr>\n", m, m));
            }
            tmp.append("</tbody></table></body></html>");
            return tmp.toString();
        }
    }

    /**
     * ��������ʱ���α�����config
     */
    public Object resetGuardedConfig(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        StringBuilder tmp = new StringBuilder("reset guarded config...\n");
        configProcessor.resetGuradedConfig(tmp);
        return tmp;
    }

    /**
     * ����ͳ��
     */
    public Object resetStat(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
//        statCmd.reset();
        return "reset stat success";
    }

    /**
     * ��ʱ����
     */
    public Object setConfig(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        StringBuilder tmp = new StringBuilder("set tmp config...\n");
        for (Map.Entry<String, List<String>> e : request.getParameters().entrySet()) {
            String fieldName = e.getKey();
            String value = e.getValue().get(0);
            configProcessor.setFieldValue(fieldName, value, tmp);
        }
        return tmp.toString();
    }

    /**
     * ˢ��dns����
     */
    public Object dnsCacheClear(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        StringBuilder tmp = new StringBuilder("before:\n");
        InetAddressCacheUtil.printCache(tmp);
        InetAddressCacheUtil.cacheClear();
        tmp.append("\n\n--------------------\nnow:\n");
        InetAddressCacheUtil.printCache(tmp);
        return tmp.toString();
    }

    public Object dnsCache(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        StringBuilder tmp = new StringBuilder();

        tmp.append("InetAddressCachePolicy.get()         = ").append(InetAddressCachePolicy.get()).append("\n");
        tmp.append("InetAddressCachePolicy.getNegative() = ").append(InetAddressCachePolicy.getNegative()).append("\n");
        tmp.append("\n");
        InetAddressCacheUtil.printCache(tmp);
        return tmp.toString();
    }
}
