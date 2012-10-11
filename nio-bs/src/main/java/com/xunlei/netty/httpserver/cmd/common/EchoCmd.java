package com.xunlei.netty.httpserver.cmd.common;

import java.util.HashMap;
import java.util.Map;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.stereotype.Service;
import com.xunlei.netty.httpserver.cmd.BaseCmd;
import com.xunlei.netty.httpserver.cmd.annotation.CmdCategory;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import com.xunlei.netty.httpserver.component.XLHttpResponse.ContentType;
import com.xunlei.spring.Config;
import com.xunlei.util.DateStringUtil;
import com.xunlei.util.DateUtil;
import com.xunlei.util.StringTools;
import com.xunlei.util.codec.DigestUtils;

/**
 * @author ZengDong
 * @since 2010-3-18 下午01:43:24
 */
@Service
@CmdCategory("system")
public class EchoCmd extends BaseCmd {

    @Config(value = "EchoCmd.now.key", resetable = true)
    String echoNowKey = "ECHONOWHASHKEY";

    public Object process(XLHttpRequest request, XLHttpResponse response) throws Exception {
        response.setInnerContentType(ContentType.plain);
        StringBuilder responseContent = new StringBuilder();
        responseContent.append("================RUIZ================\n");
        responseContent.append(request.getDetailInfo());

        // 2012-07-02 应运维的安全需求，为防止域名的恶意指向,现在针对主页的请求处理成，通过直接用ip访问时返回500
        if (request.getLocalIP().equals(request.getHeader(HttpHeaders.Names.HOST))) {
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }

        return responseContent.toString();
    }

    public String ascii(String type) throws Exception {
        // @formatter:off
     // http://www.kammerl.de/ascii/AsciiSignature.php
        String small =
                " ___   _   _   ___   ____\r\n" + 
                " | _ \\ | | | | |_ _| |_  /\r\n" + 
                " |   / | |_| |  | |   / / \r\n" + 
                " |_|_\\  \\___/  |___| /___|";
        String doom = 
                "______  _   _  _____  ______\r\n" + 
                "| ___ \\| | | ||_   _||___  /\r\n" + 
                "| |_/ /| | | |  | |     / / \r\n" + 
                "|    / | | | |  | |    / /  \r\n" + 
                "| |\\ \\ | |_| | _| |_ ./ /___\r\n" + 
                "\\_| \\_| \\___/  \\___/ \\_____/";
        String blubhead = 
                " ____  __  __  ____  ____ \r\n" + 
                "(  _ \\(  )(  )(_  _)(_   )\r\n" + 
                " )   / )(__)(  _)(_  / /_ \r\n" + 
                "(_)\\_)(______)(____)(____)";
        String isometric1 = 
                "      ___           ___                       ___     \r\n" + 
                "     /\\  \\         /\\__\\          ___        /\\  \\    \r\n" + 
                "    /::\\  \\       /:/  /         /\\  \\       \\:\\  \\   \r\n" + 
                "   /:/\\:\\  \\     /:/  /          \\:\\  \\       \\:\\  \\  \r\n" + 
                "  /::\\~\\:\\  \\   /:/  /  ___      /::\\__\\       \\:\\  \\ \r\n" + 
                " /:/\\:\\ \\:\\__\\ /:/__/  /\\__\\  __/:/\\/__/ _______\\:\\__\\\r\n" + 
                " \\/_|::\\/:/  / \\:\\  \\ /:/  / /\\/:/  /    \\::::::::/__/\r\n" + 
                "    |:|::/  /   \\:\\  /:/  /  \\::/__/      \\:\\~~\\~~    \r\n" + 
                "    |:|\\/__/     \\:\\/:/  /    \\:\\__\\       \\:\\  \\     \r\n" + 
                "    |:|  |        \\::/  /      \\/__/        \\:\\__\\    \r\n" + 
                "     \\|__|         \\/__/                     \\/__/ ";
        String soft = 
                ",------.  ,--. ,--. ,--. ,-------.\r\n" + 
                "|  .--. ' |  | |  | |  | `--.   / \r\n" + 
                "|  '--'.' |  | |  | |  |   /   /  \r\n" + 
                "|  |\\  \\  '  '-'  ' |  |  /   `--.\r\n" + 
                "`--' '--'  `-----'  `--' `-------'";
        String startwars = 
                ".______       __    __   __   ________  \r\n" + 
                "|   _  \\     |  |  |  | |  | |       /  \r\n" + 
                "|  |_)  |    |  |  |  | |  | `---/  /   \r\n" + 
                "|      /     |  |  |  | |  |    /  /    \r\n" + 
                "|  |\\  \\----.|  `--'  | |  |   /  /----.\r\n" + 
                "| _| `._____| \\______/  |__|  /________|";
        String smallant = 
                "   ___   __  __   ____  ____\r\n" + 
                "  / _ \\ / / / /  /  _/ /_  /\r\n" + 
                " / , _// /_/ /  _/ /    / /_\r\n" + 
                "/_/|_| \\____/  /___/   /___/";
        String big = 
                "  _____    _    _   _____   ______\r\n" + 
                " |  __ \\  | |  | | |_   _| |___  /\r\n" + 
                " | |__) | | |  | |   | |      / / \r\n" + 
                " |  _  /  | |  | |   | |     / /  \r\n" + 
                " | | \\ \\  | |__| |  _| |_   / /__ \r\n" + 
                " |_|  \\_\\  \\____/  |_____| /_____|";
        String standard =
                "  ____    _   _   ___   _____\r\n" + 
                " |  _ \\  | | | | |_ _| |__  /\r\n" + 
                " | |_) | | | | |  | |    / / \r\n" + 
                " |  _ <  | |_| |  | |   / /_ \r\n" + 
                " |_| \\_\\  \\___/  |___| /____|";
     // @formatter:on
        String[] asciis = { big, blubhead, doom, small, smallant, soft, standard, startwars };
        String[] asciisType = { "big", "blubhead", "doom", "small", "smallant", "soft", "standard", "startwars" };

        if (StringTools.isEmpty(type)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < asciisType.length; i++) {
                sb.append(asciisType[i]).append("\n");
                sb.append(asciis[i]).append("\n");
            }
            return sb.toString();
        } else {
            Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < asciisType.length; i++) {
                map.put(asciisType[i], asciis[i] + "\n");
            }
            return map.get(type);
        }
    }

    public Object ascii(XLHttpRequest request, XLHttpResponse response) throws Exception {
        return ascii(request.getParameter("type", null));
    }

    /**
     * <pre>
     * http://xxx.xunlei.com/echo/now?p=utc&r=xxx
     * http://xxx.xunlei.com/echo/now?p=millis&r=xxx
     * http://xxx.xunlei.com/echo/now?p=yyyyMMdd&r=xxx
     * 
     * p表示时间格式；r表示时间戳,如果有指定时间戳，则返回时间和由时间、时间戳和echoNowKey生成的md5值。时间和md5值之间用"\n"分隔。
     */
    public Object now(XLHttpRequest request, XLHttpResponse response) throws Exception {
        response.setInnerContentType(ContentType.plain);
        String pattern = request.getParameter("p", "default");
        String random = request.getParameter("r");
        StringBuilder result = new StringBuilder();
        if (pattern.equalsIgnoreCase("utc")) {
            result.append(System.currentTimeMillis() / 1000);
        } else if (pattern.equalsIgnoreCase("millis")) {
            result.append(System.currentTimeMillis());
        } else if (pattern.equalsIgnoreCase("default")) {
            result.append(DateStringUtil.getInstance(DateUtil.DF_DEFAULT).now());
        }
        if (random != null) {
            String verifyString = result.toString() + random + echoNowKey;
            String verifySecret = DigestUtils.md5Hex(verifyString.getBytes());
            result.append('\n').append(verifySecret);
        }
        return result;
    }
}
