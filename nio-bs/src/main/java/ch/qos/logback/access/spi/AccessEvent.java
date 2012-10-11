/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2009, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.access.spi;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.access.pattern.AccessConverter;
import ch.qos.logback.access.pattern.RemotePortConverter;
import ch.qos.logback.access.pattern.SimplifyResponseConverter;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import com.xunlei.netty.httpserver.util.IPGetterHelper;
import com.xunlei.util.CharsetTools;

// Contributors:  Joern Huxhorn (see also bug #110)

/**
 * <pre>
 * logback-0.9.24 -> logback-0.9.28 本类没有变化
 *  
 * 
 * The Access module's internal representation of logging events. When the logging component instance is called in the container to log then a
 * <code>AccessEvent</code> instance is created. This instance is passed around to the different logback components.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class AccessEvent implements Serializable, IAccessEvent {

    private static final long serialVersionUID = -5502700134912346943L;
    // http://www.apache-korea.org/cactus/api/framework-13/javax/servlet/ServletRequest.html#getServerName()
    public final static String[] NA_STRING_ARRAY = new String[] { IAccessEvent.NA };
    public final static String EMPTY = "";

    private transient final XLHttpRequest httpRequest;
    private transient final XLHttpResponse httpResponse;

    public static void crackTest() {
    }

    static {
        PatternLayout.defaultConverterMap.put("remotePort", RemotePortConverter.class.getName());
        PatternLayout.defaultConverterMap.put("responseContentSimple", SimplifyResponseConverter.class.getName());
    }

    String protocol;
    String method;
    String requestURL;
    String requestContent;
    String responseContent;

    /**
     * The number of milliseconds elapsed from 1/1/1970 until logging event was created.
     */
    private long timeStamp = 0;

    public AccessEvent(XLHttpRequest httpRequest, XLHttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.timeStamp = httpRequest.getCreateTime();
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        if (this.timeStamp != 0) {
            throw new IllegalStateException("timeStamp has been already set for this event.");
        }
        this.timeStamp = timeStamp;
    }

    /**
     * 取得的uri是不带?后面的参数的
     */
    @Override
    public String getRequestURI() {
        return httpRequest.getPath();
    }

    /**
     * The first line of the request.
     */
    @Override
    public String getRequestURL() {
        if (requestURL == null) {
            StringBuilder buf = new StringBuilder();
            buf.append(httpRequest.getMethod());
            buf.append(AccessConverter.SPACE_CHAR);
            buf.append(httpRequest.getUri());
            buf.append(AccessConverter.SPACE_CHAR);
            buf.append(httpRequest.getProtocolVersion());
            requestURL = buf.toString();
        }
        return requestURL;
    }

    @Override
    public String getRemoteHost() {
        return httpRequest.getRemoteHost();
    }

    /**
     * <pre>
     * Returns the login of the user making this request, if the user has been authenticated,
     * or null if the user has not been authenticated.
     * Whether the user name is sent with each subsequent request depends on the browser and type of authentication.
     * Same as the value of the CGI variable REMOTE_USER.
     * 
     * 此方法只适用于 servlet带session情况,因此忽略!?
     * </pre>
     * 
     * @return
     */
    @Override
    public String getRemoteUser() {
        return IAccessEvent.NA;
    }

    @Override
    public String getProtocol() {
        if (protocol == null) {
            HttpVersion v = httpRequest.getProtocolVersion();
            if (v != null) {
                String t = v.getText();
                if (t == null || t.isEmpty()) {
                    protocol = IAccessEvent.NA;
                } else {
                    protocol = t;
                }
            } else {
                protocol = IAccessEvent.NA;
            }
        }
        return protocol;
    }

    @Override
    public String getMethod() {
        if (method == null) {
            HttpMethod m = httpRequest.getMethod();
            if (m == null) {
                method = IAccessEvent.NA;
            } else {
                method = m.toString();
            }
        }
        return method;
    }

    /**
     * <pre>
     * Returns the host name of the server that received the request.
     * For HTTP servlets, same as the value of the CGI variable SERVER_NAME.
     * 
     * 没什么意义,先不实现
     * </pre>
     * 
     * @return
     */
    @Override
    public String getServerName() {
        return IAccessEvent.NA;
    }

    @Override
    public String getRemoteAddr() {
        String r = IPGetterHelper.getIP(httpRequest);
        if (r.isEmpty()) {
            return IAccessEvent.NA;
        }
        return r;
    }

    @Override
    public String getRequestHeader(String key) {
        // key = key.toLowerCase();
        String result = httpRequest.getHeader(key);
        if (result != null) {
            return result;
        }
        return IAccessEvent.NA;
    }

    /**
     * DBAppender跟FullRequestConverter会用到
     */
    @Override
    public Enumeration<String> getRequestHeaderNames() {
        // return new Vector<String>(httpRequest.getHeaderNames()).elements();
        throw new UnsupportedOperationException("getRequestHeaderNames");
    }

    /**
     * RequestHeaderConverter会用到
     */
    @Override
    public Map<String, String> getRequestHeaderMap() {
        throw new UnsupportedOperationException("getRequestHeaderMap");
    }

    /**
     * Attributes are not serialized
     * 
     * @param key
     */
    @Override
    public String getAttribute(String key) {
        throw new UnsupportedOperationException("getAttribute");
    }

    /**
     * RequestParameterConverter会用到
     */
    @Override
    public String[] getRequestParameter(String key) {
        throw new UnsupportedOperationException("getRequestParameter");
    }

    @Override
    public String getCookie(String key) {
        Cookie c = httpRequest.getCookie(key);
        if (c != null) {
            return c.getValue();
        }
        return IAccessEvent.NA;
    }

    @Override
    public long getContentLength() {
        return httpResponse.getContentLength();
    }

    @Override
    public int getStatusCode() {
        HttpResponseStatus status = httpResponse.getStatus();
        if (status != null) {
            return status.getCode();
        }
        return SENTINEL;
    }

    @Override
    public String getRequestContent() {
        if (requestContent != null) {
            return requestContent;
        }
        if (HttpMethod.POST.equals(httpRequest.getMethod()) && "application/x-www-form-urlencoded".equals(httpRequest.getHeader(HttpHeaders.Names.CONTENT_TYPE))) {
            return new String(httpRequest.getContent().toString(CharsetTools.UTF_8));// TODO:如果处理charset
            // return httpRequest.getPostContentDecoder().getPostContent();
        }
        StringBuilder tmp = new StringBuilder();
        Map<String, List<String>> params = httpRequest.getParameters();
        for (Entry<String, List<String>> p : params.entrySet()) {
            String key = p.getKey();
            List<String> vals = p.getValue();
            for (String val : vals) {
                tmp.append("&").append(key).append('=').append(val);
            }
        }
        if (tmp.length() > 0) {
            requestContent = tmp.substring(1);
        } else {
            requestContent = "";
        }
        return requestContent;
    }

    @Override
    public String getResponseContent() {
        if (responseContent != null) {
            return responseContent;
        }
        String responseType = httpResponse.getHeader(HttpHeaders.Names.CONTENT_TYPE);
        if ((responseType != null) && (responseType.startsWith("image/"))) {
            responseContent = "[IMAGE CONTENTS SUPPRESSED]";
        } else if (getStatusCode() == HttpResponseStatus.FOUND.getCode()) { // 如果是通过redirect跳转的，此处内容就是跳转后的URL
            responseContent = httpResponse.getHeader(HttpHeaders.Names.LOCATION);
        } else {
            responseContent = httpResponse.getContentString();
        }
        return responseContent;
    }

    @Override
    public int getLocalPort() {
        return httpRequest.getLocalPort();
    }

    public int getRemotePort() {
        return httpRequest.getRemotePort();
    }

    @Override
    public String getResponseHeader(String key) {
        String r = httpResponse.getHeader(key);
        if (r == null || r.isEmpty()) {
            return IAccessEvent.NA;
        }
        return r;
    }

    /**
     * 用于FullResponseConverter
     */
    @Override
    public List<String> getResponseHeaderNameList() {
        throw new UnsupportedOperationException("getResponseHeaderNameList");
    }

    @Override
    public void prepareForDeferredProcessing() {
        // buildRequestHeaderMap();
        // buildRequestParameterMap();
        // buildResponseHeaderMap();

        // getLocalPort();
        // getMethod();
        // getProtocol();
        // getRemoteAddr();
        // getRemoteHost();
        // getRemoteUser();
        // getRequestURI();
        // getRequestURL();
        // getServerName();
        // getTimeStamp();

        // getStatusCode();
        // getContentLength();
        // getRequestContent();
        // getResponseContent();
    }

    // 以下两个是自己要用的
    public XLHttpRequest getHttpRequest() {
        return httpRequest;
    }

    public XLHttpResponse getHttpResponse() {
        return httpResponse;
    }

    // 以下方法是 0.9.28变成1.0.0时，多出来的方法，不用先
    // TODO:后续可以再整理，如让httpRequest跟servlet规范对接
    @Override
    public Map<String, String[]> getRequestParameterMap() {
        throw new UnsupportedOperationException("getRequestParameterMap");
    }

    @Override
    public ServerAdapter getServerAdapter() {
        throw new UnsupportedOperationException("getServerAdapter");
    }

    @Override
    public Map<String, String> getResponseHeaderMap() {
        throw new UnsupportedOperationException("getResponseHeaderMap");
    }

    @Override
    public HttpServletRequest getRequest() {
        throw new UnsupportedOperationException("getRequest");
    }

    @Override
    public HttpServletResponse getResponse() {
        throw new UnsupportedOperationException("getResponse");
    }
}