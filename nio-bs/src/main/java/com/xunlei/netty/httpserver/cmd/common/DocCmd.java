package com.xunlei.netty.httpserver.cmd.common;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xunlei.netty.httpserver.cmd.BaseStatCmd;
import com.xunlei.netty.httpserver.cmd.CmdMappers;
import com.xunlei.netty.httpserver.cmd.CmdMappers.CmdMeta;
import com.xunlei.netty.httpserver.cmd.annotation.Cmd;
import com.xunlei.netty.httpserver.cmd.annotation.CmdAuthor;
import com.xunlei.netty.httpserver.cmd.annotation.CmdCategory;
import com.xunlei.netty.httpserver.cmd.annotation.CmdContentType;
import com.xunlei.netty.httpserver.cmd.annotation.CmdParam;
import com.xunlei.netty.httpserver.cmd.annotation.CmdParams;
import com.xunlei.netty.httpserver.cmd.annotation.CmdReturn;
import com.xunlei.netty.httpserver.cmd.annotation.CmdSession;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import com.xunlei.netty.httpserver.component.XLHttpResponse.ContentType;
import com.xunlei.netty.httpserver.util.CmdSessionType;
import com.xunlei.util.EmptyChecker;
import com.xunlei.util.StringTools;

/**
 * ���߽ӿ��ĵ�
 * 
 * @author ZengDong
 * @since 2010-5-23 ����12:15:48
 */
@Service
@CmdCategory("system")
public class DocCmd extends BaseStatCmd {

    // private static final Logger log = Log.getLogger();
    @Autowired
    private CmdMappers cmdMappers;
    // @formatter:off
    private final String head = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head>";
    private final String js = "<script type=\"text/javascript\">function showul(element){var ul = get_nextsibling(element);if(ul.style.display==\"block\")ul.style.display=\"none\";else{ul.style.display=\"block\";}}\nfunction get_nextsibling(n){var x=n.nextSibling;while (x.nodeType!=1){x=x.nextSibling;}return x;}\nfunction showdesc(elementId){var desc = document.getElementById(elementId);var descs = getElementsByClass(\"desc\",\"div\");for(var i=0;i<descs.length;i++){if(descs[i]!=desc && descs[i].style.display==\"block\")descs[i].style.display = \"none\";}if(desc.style.display==\"none\"){desc.style.display=\"block\";desc.scrollIntoView();}}"
            + "\nfunction getElementsByClass(searchClass,tag) {var classElements = new Array();if ( tag == null )tag = '*';var els = document.getElementsByTagName(tag);var elsLen = els.length;var pattern = new RegExp(\"(^|\\s)\"+searchClass+\"(\\s|$)\");for (i = 0, j = 0; i < elsLen; i++) {if ( pattern.test(els[i].className) ) {classElements[j] = els[i];j++;}}return classElements;}</script>";
    private final String css =
            "<style type=\"text/css\">\n" +
            "body,dl,dt,dd,ul,ol,li,h1,h2,h3,h4,h5,h6,pre,form,fieldset,input,textarea,p,th,td{padding:0;margin:0;} " +
            "table{border-collapse:collapse;border-spacing:0;} " 
            + "html,body{width:100%;overflow:auto;}" +
            "" +
            "body{\n" +
            "    margin:0px;\n" +
            "    font: normal 12px/1.6em simsun;"+
            "    }\n" +
            "#header{\n" +
            "    border-bottom:1px #CCCCCC dashed;\n" +
            "    color:#999;\n" +
            "    text-align:center;\n" +
            "    margin-bottom:10px;\n" +
            "    }\n" +
            "#wrapper{\n" +
            "    width:100%;\n" +
            "    }\n" +
            "#sidebar{\n" +
            "    float: left;\n" +
            "    width: 100%;\n" +
            "    overflow-x:hidden;" +
            "    overflow-y:auto;"+
            "    }\n" +
            "h2{\n" +
            "    cursor: pointer;\n" +
            "    font-size: 12px;\n" +
            "    font-weight:400;\n" +
            "    padding: 4px;\n" +
            "    text-align: center;\n" +
            "    margin:0px;\n" +
            "}\n" +
            "h2:hover {\n" +
            "    background: none repeat scroll 0 0 #4F81BD;\n" +
            "    color: white;\n" +
            "}\n" +
            "#sidebar ul{\n" +
            "    margin: 0;\n" +
            "    background:none repeat scroll 0 0 white;\n" +
            "    display:none;\n" +
            "    }\n" +
            "li{\n" +
            "    list-style:none;\n" +
            "    }\n" +
            "li h2{\n" +
            "    padding-left:20px;\n"+
            "    text-align:left;\n" +
            "    background-color:white;\n" +
            "    }\n" +
            ".desc{\n" +
            "    float:right;\n" +
            "    width:100%;\n"+
            "    margin-bottom: 50px;\n" +
            "    word-wrap:break-word;" +
            "    word-break:normal;"+
            "    }\n" +
            "#content h3 {\n" +
            "    background: none repeat scroll 0 0 #95B3D7;\n" +
            "    color: #FFFFFF;\n" +
            "    margin: 5px auto;\n" +
            "    padding: 5px;\n" +
            "    font-size:12px;\n" +
            "    font-weight:400;\n" +
            "    overflow:hidden;\n" +
            "}\n" +
            "#content h2 {\n" +
            "    background: none repeat scroll 0 0 #4F81BD;\n" +
            "    color: #FFFFFF;\n" +
            "    font-size: 14px;\n" +
            "    font-weight: bold;\n" +
            "    margin: 0px auto;\n" +
            "    padding: 5px;\n" +
            "    text-align:left;\n" +
            "}\n" +
            "#content h4 {\n" +
            "    background: none repeat scroll 0 0 #DBE5F1;\n" +
            "    margin: 5px auto 5px 5px;\n" +
            "    padding: 5px;\n" +
            "    font-weight:400;\n" +
            "}\n" +
            "table{\n" +
            "    width:100%;\n" +
            "    margin:0px;\n" +
            "    padding:0px;\n" +
            "    }\n" +
            "td{\n" +
            "    width:20%;\n" +
            "    margin-left:5px;\n" +
            "    }\n" +
            "#content span{\n" +
            "    display:block;\n" +
            "    margin-left:30px;\n" +
            "    }\n" +
            "#content h3 table td{font-size:12px;color: #FFFFFF;}" +
            "#content h4 table td{font-size:12px;}" +
            "a{text-decoration:none;color:#000;}" +
            "#content .desc a h2:hover{}" +
            "#content .desc a:link{color:#000}" +
            "</style>\n";
    // @formatter:on
    private String all_doc;
    private String all_system_doc;

    public String getAll_doc() {
        if (all_doc == null) {
            all_doc = getDoc(false);
        }
        return all_doc;
    }

    public String getAll_system_doc() {
        if (all_system_doc == null) {
            all_system_doc = getDoc(true);
        }
        return all_system_doc;
    }

    public String getDoc(boolean system) {
        Map<CmdMeta, List<String>> cmd_urls_map = cmdMappers.getReverseCmdAllSortedMap();
        Map<String, List<Entry<CmdMeta, List<String>>>> category_cmds_map = new LinkedHashMap<String, List<Entry<CmdMeta, List<String>>>>();
        for (Entry<CmdMeta, List<String>> e : cmd_urls_map.entrySet()) {
            CmdMeta meta = e.getKey();
            for (String category : meta.getCategories()) {
                boolean hasDoc = false;
                if ("system".equals(category) ^ system) { // �����Ƿ�Ҫ��ʾϵͳ����
                    continue;
                }
                for (Annotation a : meta.getMethod().getAnnotations()) {
                    if (a.toString().startsWith("@com.xunlei.netty.httpserver.cmd.annotation")) {// ˵����cmd���ĵ�ע��
                        hasDoc = true;
                        break;
                    }
                }
                if (hasDoc) {
                    String key = category + " " + meta.getCmdDescription();
                    List<Entry<CmdMeta, List<String>>> cmds = category_cmds_map.get(key);
                    if (cmds == null) {
                        cmds = new ArrayList<Map.Entry<CmdMeta, List<String>>>(1);
                        category_cmds_map.put(key, cmds);
                    }
                    cmds.add(e);
                }
            }
        }

        StringBuilder tmp = new StringBuilder(head);
        tmp.append(css);
        tmp.append(js);
        tmp.append("</head><body>");
        tmp.append("<div id=\"wrapper\"><div id=\"roller\" style=\"height:100%;width:350px;position:fixed;overflow:auto;left:0;background-color:#DBE5F1;\"><div id=\"sidebar\">");
        for (Entry<String, List<Entry<CmdMeta, List<String>>>> e : category_cmds_map.entrySet()) {
            String category = e.getKey();
            tmp.append("<h2 onclick=\"showul(this)\">").append(category).append("</h2>");
            tmp.append("<ul>");
            List<Entry<CmdMeta, List<String>>> cmdProperty = e.getValue();
            for (Entry<CmdMeta, List<String>> cmd : cmdProperty) {
                // CmdMeta meta = cmd.getKey();
                List<String> urls = cmd.getValue();
                for (String url : urls) {
                    tmp.append("<a href=\"#").append(url).append("\">").append("<li><h2>").append(url).append("</h2></a></li>");
                }
            }
            tmp.append("</ul>");
        }
        tmp.append("</div></div>");
        tmp.append("<div id=\"content\" style=\"margin:0 10px 0 360px;\">");
        for (Entry<String, List<Entry<CmdMeta, List<String>>>> e : category_cmds_map.entrySet()) {
            List<Entry<CmdMeta, List<String>>> cmdProperty = e.getValue();
            for (Entry<CmdMeta, List<String>> cmd : cmdProperty) {
                List<String> urls = cmd.getValue();
                for (String url : urls) {
                    CmdMeta meta = cmd.getKey();
                    Cmd c = meta.getMethod().getAnnotation(Cmd.class);
                    CmdAuthor author = meta.getMethod().getAnnotation(CmdAuthor.class);
                    CmdContentType contentType = meta.getMethod().getAnnotation(CmdContentType.class);
                    CmdParams params = meta.getMethod().getAnnotation(CmdParams.class);
                    CmdReturn ret = meta.getMethod().getAnnotation(CmdReturn.class);
                    CmdSession session = meta.getMethod().getAnnotation(CmdSession.class);
                    tmp.append("<div class=\"desc\" id=\"").append(url).append("\">");
                    if (c != null) {
                        tmp.append("<a href=\"").append(url).append("\" target=\"_blank\"><h2>").append(c.value()).append("\t").append(url).append("</h2>").append("</a>");
                    }
                    tmp.append("<h3><table><tr><td>��¼̬Ҫ��</td><td nowrap>");
                    if (null == session) {
                        tmp.append("δ֪");
                    } else {
                        if (session.type() == CmdSessionType.COMPELLED) {
                            tmp.append("����Ҫ���е�¼̬������rtn:11");
                        } else if (session.type() == CmdSessionType.NOT_COMPELLED) {
                            tmp.append("����Ҫ��¼̬");
                        } else if (session.type() == CmdSessionType.DISPENSABLE) {
                            tmp.append("Ҫ���е�¼̬��û��Ҳ���ᱨ�����������οʹ���");
                        }
                    }
                    tmp.append("</td><td></td><td></td><td nowarp>");
                    if (author != null) {
                        printArray(tmp, author.value());
                    }
                    tmp.append("</td></tr></table></h3>");
                    if (c != null) {
                        String[] descs = c.desc();
                        if (EmptyChecker.isNotEmpty(descs) && (descs.length > 1 || StringTools.isNotEmpty(descs[0]))) {
                            tmp.append("<span>");
                            for (String d : descs) {
                                tmp.append("\n").append(d);
                            }
                            tmp.append("</span>");
                        }
                    }
                    tmp.append("<h3>����</h3>");

                    if (params != null) {
                        for (CmdParam p : params.value()) {
                            tmp.append("<h4><table><tr><td>").append(p.name()).append("</td><td>").append(p.type().getSimpleName()).append("</td><td>").append(p.compelled() ? "����" : "")
                                    .append("</td><td>").append(StringTools.isEmpty(p.defaultValue()) ? "" : "Ĭ��ֵ:" + p.defaultValue()).append("</td><td>");
                            printArray(tmp, p.scope());
                            tmp.append("</td></tr></table></h4>").append("<span>");
                            String[] descs = p.desc();
                            for (int i = 0; i < descs.length; i++) {
                                tmp.append(StringTools.escapeHtml(descs[i])).append("\n<br/>");
                                // tmp.append(descs[i]).append("\n");
                            }
                            tmp.append("</span>");
                        }
                    }
                    tmp.append("<h3><table><tr><td>�ذ�</td><td>");
                    if (contentType != null) {
                        printArray(tmp, contentType.value());
                    }
                    tmp.append("</td><td></td><td></td><td></td></tr></table></h3><span>");
                    if (ret != null) {
                        String[] descs = ret.value();
                        if (EmptyChecker.isNotEmpty(descs) && (descs.length > 1 || StringTools.isNotEmpty(descs[0]))) {
                            // tmp.append("<pre>");
                            for (String d : descs) {
                                tmp.append(StringTools.escapeHtml(d)).append("\n<br/>");
                            }
                            // tmp.append("</pre>");
                        }
                    }
                    tmp.append("</span></div>");
                }
            }
        }
        tmp.append("</div>");
        tmp.append("</div>");
        tmp.append("</body></html>");
        return tmp.toString();
    }

    /**
     * ��ʾ��ǰ�Ѿ�ӳ�������
     */
    public Object process(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        response.setInnerContentType(ContentType.html);
        return getAll_doc();
    }

    public Object system(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        response.setInnerContentType(ContentType.html);
        return getAll_system_doc();
    }

    private void printArray(StringBuilder tmp, Object[] arr) {
        // tmp.append("<pre>");
        boolean first = true;
        if (EmptyChecker.isNotEmpty(arr)) {
            for (Object au : arr) {
                String a = StringTools.escapeHtml(au.toString());
                if (!first) {
                    tmp.append(",").append(a);
                } else {
                    tmp.append(a);
                    first = false;
                }

            }
        }
        // tmp.append("</pre>");
    }

}
