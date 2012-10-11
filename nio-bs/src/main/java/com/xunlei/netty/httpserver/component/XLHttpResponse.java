package com.xunlei.netty.httpserver.component;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpMessage;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import com.xunlei.netty.httpserver.util.HttpServerConfig;
import com.xunlei.util.CharsetTools;

/**
 * ���� ���ܣ�setStatus,setHeaderIfEmpty ����addCookie�ķ��� ����innserContentType,����Ӧjson,��xml�ķ�װ
 * 
 * @author ZengDong
 * @since 2010-3-25 ����02:03:41
 */
public class XLHttpResponse extends DefaultHttpMessage implements HttpResponse {

    public enum ContentType {
        html, json, plain, xml
    }

    private Charset contentCharset = CharsetTools.UTF_8;// Ĭ����utf8,���޸ĵĻ�ʹ��setContentCharset
    private int contentLength = -1;
    private boolean contentSetted = false;
    private List<Cookie> cookies = new ArrayList<Cookie>(1);
    private long createTime = System.currentTimeMillis();
    private ContentType innerContentType = HttpServerConfig.getRespInnerContentType();
    private int keepAliveTimeout = HttpServerConfig.getKeepAliveTimeout();
    private boolean keepAliveTimeoutSetted = false;
    private HttpResponseStatus status;
    private XLContextAttachment attach;
    private String contentString;

    public XLHttpResponse(XLContextAttachment attach) {
        super(HttpVersion.HTTP_1_1);
        // 2012-4-14 zengdong ���������£������ܷ���android�����������pipelineģʽ
        // 2012-4-19 �����ϲ��Է��� �ĳ� http/1.0 ��Щ�������Ȼ���Ƿ�pipeline����> <
        this.status = HttpResponseStatus.OK;
        this.attach = attach;
    }

    public XLHttpResponse() {// ��ʼ��һЩĬ��ֵ
        super(HttpVersion.HTTP_1_1);
        this.status = HttpResponseStatus.OK;
    }

    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }

    public Charset getContentCharset() {
        return contentCharset;
    }

    public String getContentString() {// һ������ �ڲ��鿴
        if (contentString == null) {
            ChannelBuffer cb = this.getContent();
            if (cb == null) {
                return null;
            } else {
                return cb.toString(contentCharset);
            }
        }
        return contentString;
    }

    public long getContentLength() {
        return contentLength;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public long getCreateTime() {
        return createTime;
    }

    public ContentType getInnerContentType() {
        return innerContentType;
    }

    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public HttpResponseStatus getStatus() {
        return this.status;
    }

    public boolean isContentSetted() {
        return contentSetted;
    }

    /**
     * 302�ض���
     * 
     * @param localtionUrl �ض����URL
     */
    public void redirect(String localtionUrl) {
        setStatus(HttpResponseStatus.FOUND);
        setHeaderIfEmpty(HttpHeaders.Names.LOCATION, localtionUrl);
    }

    /**
     * �ض���
     * 
     * @param localtionUrl �ض����URL
     */
    public void redirect(String localtionUrl, HttpResponseStatus status) {
        setStatus(status);
        setHeaderIfEmpty(HttpHeaders.Names.LOCATION, localtionUrl);
    }

    @Override
    public void setContent(ChannelBuffer content) {
        super.setContent(content);
        if (content != null)
            contentLength = content.readableBytes();
        contentSetted = true;
    }

    public void setContentCharset(Charset contentCharset) {
        this.contentCharset = contentCharset;
    }

    public void setContentString(String contentStr) {
        this.contentString = contentStr;// ������� deflate����ʱ��HttpContentCompressor ���ڱ���ʱsetContent(channelBuffer) Ϊ����accesslog������ʾ��������ǰ���� contentStr
        setContent(ChannelBuffers.copiedBuffer(contentStr, contentCharset));
    }

    /**
     * ע�ⲻҪ����������keepAlive��ز���,Ҫ������ʹ��setKeepAliveTimeout
     */
    public boolean setHeaderIfEmpty(String name, String value) {
        if (getHeader(name) == null) {
            setHeader(name, value);
            return true;
        }
        return false;
    }

    public void setInnerContentType(ContentType innerContentType) {
        this.innerContentType = innerContentType;
    }

    public void setKeepAliveTimeout(int keepAliveTimeout) {
        if (keepAliveTimeout < 0) {
            throw new IllegalArgumentException("keepAliveTimeout:" + keepAliveTimeout + " cant be nagative");
        }
        this.keepAliveTimeoutSetted = true;
        this.keepAliveTimeout = keepAliveTimeout;
    }

    public void setStatus(HttpResponseStatus status) {
        this.status = status;
    }

    public String toString() {
        return getProtocolVersion().getText() + ' ' + getStatus().toString();
    }

    @Override
    public void setChunked(boolean chunked) {
        super.setChunked(chunked);
        if (chunked) {// ���Է������������header,�������һֱ �ȴ�(תȦȦ)
            this.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
        } else {
            this.removeHeader(HttpHeaders.Names.TRANSFER_ENCODING);
        }
    }

    /**
     * <pre>
     * ���������úõ� cookies��� ʵ��Ҫ������Ӧʱ�õ���set-cookie: ��Ӧͷ
     * 
     * �˷��������ڲ�ʹ��
     */
    public void packagingCookies() {
        List<Cookie> cookies = getCookies();
        if (!cookies.isEmpty()) {
            // Reset the cookies if necessary.
            for (Cookie cookie : cookies) {
                CookieEncoder cookieEncoder = new CookieEncoder(true);
                cookieEncoder.addCookie(cookie);
                addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
            }
        }
    }

    public boolean isKeepAliveTimeoutSetted() {
        return keepAliveTimeoutSetted;
    }

    public XLContextAttachment getAttach() {
        return attach;
    }
}
