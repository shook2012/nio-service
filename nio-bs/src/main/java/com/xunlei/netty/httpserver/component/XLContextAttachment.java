package com.xunlei.netty.httpserver.component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import com.xunlei.netty.httpserver.cmd.CmdMappers.CmdMeta;
import com.xunlei.netty.httpserver.exception.ClosedChannelError;
import com.xunlei.netty.httpserver.exception.ProcessTimeoutError;
import com.xunlei.util.CloseableHelper;
import com.xunlei.util.DateStringUtil;
import com.xunlei.util.EmptyChecker;
import com.xunlei.util.HumanReadableUtil;
import com.xunlei.util.Log;
import com.xunlei.util.StringHelper;

/**
 * <pre>
 * ��ʼ����		attach.decode (req.createTime)		
 * 									} ������ʱ
 * ��ʼҵ����	attach.process (resp.createTime)
 * 									} ҵ������ʱ
 * ��ʼ����		attach.encode
 * 									} ������ʱ
 * �������		attach.complete
 * 
 * 2011-03-19 �����Ż�attach
 * 
 * �ھ����ҵ��disaptcher��
 * 
 * channelOpenʱ,�½�attach ����ֱ��ע��һ���������ڵ�context  ��ѡ��[channelBound mark lastIoTime��[channelConnʱ,˵����׼�� decode mark lastIoTime]
 * messageReceivedʱ,˵����decode���,mark lastIoTime (������ͨ��httpReq���ҵ���decode�Ŀ�ʼʱ��,��resp��createTime���Ǹ���ǰlastIoTimeһ��),��ע�����µ�httpReq,httpResp(����ע�����µ�messageEvent)
 * 
 * disaptchʱ,ע����ҵ���߳�,˵�����߳̿��ܻ��г�ʱ��ҵ�����
 * ҵ�������ʱ,ע����ҵ���߳�
 * ����wirteResponseʱ,���俪ʼencode���ź�,���mark lastIoTime(д)
 * 
 * writeCompleteʱ,˵�����Ѿ�encode���,mark lastIoTime(д)
 *  
 * channelCloseʱ,ֱ��ע����attach,���������������յ�  
 *                 
 * 
 * ���ⲿ��������,������ҵ���߳�,��ͨ���ж�cmdMeta��timeout���ж��Ƿ��ж�
 * û��ҵ���߳�,�����ж���ioTime  
 * 
 * @author ZengDong
 * @since 2010-5-22 ����09:29:54
 */
public class XLContextAttachment implements ChannelFutureListener, Comparable<XLContextAttachment> {

    private static final Logger log = Log.getLogger();

    /**
     * ���������һ�ε�ǰchannel��request����
     */
    private XLHttpRequest request;
    /**
     * ���������һ�ε�ǰchannel��request����
     */
    private XLHttpResponse response;
    /**
     * attach��ctx������������һ�µ�,�ڳ�ʼ��ʱ��
     */
    private ChannelHandlerContext channelHandlerContext;
    // private MessageEvent messageEvent;
    /**
     * �������һ����dispatch��ʹ�õ�cmdMeta
     */
    private CmdMeta cmdMeta;

    private long channelOpenTime;
    /**
     * ��ǰchannel��֪�ϴζ�ʱ���
     */
    private long lastReadTime;
    /**
     * ��ǰchannel��֪�ϴ�дʱ���
     */
    private long lastWriteTime;

    public long markLastReadTime() {
        lastReadTime = System.currentTimeMillis();
        return lastReadTime;
    }

    /**
     * ������ҵ���߳�
     */
    private volatile Thread processThread;

    /*
     * decode process encode complete ��Ϊ��ͳ��ʹ��
     */
    private long decode;
    private long process;
    private long encode;
    private long complete;

    /**
     * <pre>
     * �������һ��������ҵ����ʱ��Ҫ����close�Ķ���
     * 
     * ��TimeoutThreadInterrupter�����ж�ʱ,Ҳ��Ѵ������ڵ����ж������close
     */
    private Set<Object> closeable;
    /**
     * <pre>
     * �������һ������
     * �ڴ��������,��������ʱ,���ռ�������
     */
    private List<Throwable> throwables;
    /**
     * ���ڱ�����ҵ����������Ҫ������м��������ж�����ڲ�������Object[]/List<Object>/Map<String,Object>�����
     */
    private Object innerAttach;
    private MessageEvent[] asyncMessageEventQueue;
    private int asyncMessageEventQueueCounter;
    private volatile boolean running;// attach��channelOpen ��channelWriteComplete�����Ϊrunning
    private boolean closeAfterOperationComplete = true;
    private int respOperationCompleteCount = 0; // 2012-06-01 ��ǰattach���������˼����ذ������ڴ�keep-aliveʱ��ͳ�ƻذ���
    /**
     * <pre>
     * http://www.blaze.io/mobile/http-pipelining-big-in-mobile/
     * 
     * Recovery On ��Connection: Close��
     * 
     * When using pipelining, the server may always close the connection before all requests are fulfilled.
     * For example, if requests 1 and 2 are sent on the same connection, the response to request 1 may include
     * a ��Connection: close�� header. In this case, the browser will honor the close instruction and close the
     * connection, without a response to request 2, and resent request two on a different connection.
     * It��s worth noting that getting such a ��Connection: Close�� will not make Opera stop using pipelining on
     * that host, even if all future requests come back with such a ��close�� header.
     */
    private boolean userAgentTryPipelining = false;
    private static final String HTTP_HEADER_KEEP_ALIVE = "Keep-Alive";

    public void initAsyncMessageEventQueue(int concurrentNum) {
        if (concurrentNum > 0) {
            this.asyncMessageEventQueue = new MessageEvent[concurrentNum];
            this.asyncMessageEventQueueCounter = concurrentNum;
        }
    }

    /**
     * ���沢����õ��첽messageEvent,���������������Ӧ����᷵��true�������п���ͬʱ�������ؽ������˴�Ӧ������ͬ������
     */
    public synchronized boolean asyncMessageEventReceived(MessageEvent e) {
        if (asyncMessageEventQueue == null) { // ���û�г�ʼ������Ĭ����1��
            initAsyncMessageEventQueue(1);
        }
        int idx = --asyncMessageEventQueueCounter;
        asyncMessageEventQueue[idx] = e;
        return idx == 0;
    }

    public MessageEvent[] getAsyncMessageEventQueue() {// TODO:������ʱû�а�message��Ӧ�� ChannelHandlerContext ctx���������Ժ�������ʱ�����Լ���
        return asyncMessageEventQueue;
    }

    public void registerInnerAttach(Object _innerAttach) {
        this.innerAttach = _innerAttach;
    }

    @SuppressWarnings("unchecked")
    public <T> T getInnerAttach() {
        return (T) innerAttach;
    }

    public void registerThrowable(Throwable ex) {
        if (throwables == null) {
            throwables = new ArrayList<Throwable>(1);
        }
        throwables.add(ex);
    }

    /**
     * �����Ƿ��ڹ涨ʱ���ڴ���������
     * 
     * @param timeoutms ��ʱʱ������λms
     */
    public boolean isTimeout(long timeoutms) {
        return System.currentTimeMillis() - decode > timeoutms;
    }

    public void checkChannelOrThread() {// ҵ���߳�����check
        boolean shutdown = false;
        try {
            shutdown = !channelHandlerContext.getChannel().isOpen();
            if (shutdown) {
                throw ClosedChannelError.INSTANCE;
                // } else if (shutdown = (processThread != null && processThread.isInterrupted()) || Thread.currentThread().isInterrupted()) {
            }
            if (processThread != null) {
                shutdown = processThread.isInterrupted(); // 2012-04-16 zengdong ���ﲻ���� Thread.interrupt��Ϊ��Ҫ�жϵ��� ���ڴ�����߳�
                if (shutdown) {
                    throw ProcessTimeoutError.INSTANCE;
                }
            }
        } finally {
            if (shutdown) {
                // �����Ѿ�����,�����Ѿ����ж���
                closeCloseable();
            }
        }
    }

    private synchronized void closeCloseable() {
        if (closeable != null) {
            for (Object obj : closeable) {
                if (obj instanceof Thread) {
                    ((Thread) obj).interrupt();
                } else {
                    CloseableHelper.closeSilently(obj);
                }
            }
        }
        closeable = null;
    }

    /**
     * ��ͨ���ر�ʱ֪ͨ��attach�����жϼ��ر�
     * 
     * @return true��ʾ���� �հ�->�����м䱻�жϵ�(һ������peer��)��false��ʾ�������
     */
    // ����Ҫͬ����ԭ���������жϼ�϶��thread��ҵ��ʹ��,���interruptһ���������߳���
    public synchronized boolean interrupt(StringBuilder info) {// �ⲿsweeper�������ж�
        if (running && processThread != null) {
            processThread.interrupt();
            if (info != null) {
                info.append("[processThread]").append(processThread.getName());
            }
        }
        if (closeable != null && info != null) {
            info.append("[closeable]");
            for (Object obj : closeable) {
                info.append(obj).append(" ");
            }
        }
        closeCloseable();
        return running;
    }

    /**
     * �ж��Ƿ���ҵ����
     */
    public boolean isNotProcessing() {
        // return processThread == null && EmptyChecker.isEmpty(closeable);
        // 2012-05-31 �ж��Ƿ���ҵ����Ӧ���Ǵ� �յ�req��������resp֮�䣬Ҳ���� running
        return !running;
    }

    public synchronized void _registerCloseable(Object obj) {
        if (closeable == null) {
            closeable = new HashSet<Object>(1);
        }
        closeable.add(obj);
    }

    public void registerCloseable(Object obj) {
        _registerCloseable(obj);
    }

    public void registerCloseable(Thread t) {
        Thread.interrupted();// ���õ�ǰ�̵߳��жϱ�־λ
        _registerCloseable(t);
    }

    public synchronized void unregisterCloseable(Object obj) {
        if (closeable != null) {
            closeable.remove(obj);
        }
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();
        if (respOperationCompleteCount > 0) {
            sb.append("[").append(respOperationCompleteCount).append("]");
        }
        sb.append(cmdMeta);
        sb.append(request == null ? "" : "-" + Integer.toHexString(request.hashCode()));
        sb.append(channelHandlerContext.getChannel().getRemoteAddress());
        sb.append("/");
        getTimeSpanInfo(sb, System.currentTimeMillis(), false);
        return sb.toString();
    }

    public StringBuilder getTimeSpanInfo() {// ����debugʱ��ӡ����Ϣ
        return getTimeSpanInfo(new StringBuilder(), System.currentTimeMillis(), true);
    }

    public StringBuilder getTimeInfo(StringBuilder tmp, String name, long time) {
        if (time > 0) {
            tmp.append(name).append(DateStringUtil.DEFAULT.format(new Date(time))).append("\t");
        }
        return tmp;
    }

    public StringBuilder getTimeSpanInfo(StringBuilder tmp, long now, boolean fmt) {
        long decode_end = decode > 0 ? decode : now;
        long process_end = process > 0 ? process : now;
        long encode_end = encode > 0 ? encode : now;
        long complete_end = complete > 0 ? complete : now;

        String stage = "N/A";
        if (complete > 0) {
            stage = "complete";
        } else if (encode > 0) {
            stage = "encode";
        } else if (process > 0) {
            stage = "process";
        } else if (decode > 0) {
            stage = "decode";
        }

        String timeSpanInfoFmt = fmt ? "%-8s %-7s(%s|%s,%s,%s)" : "%s:%s(%s|%s,%s,%s)";
        String all_str = fmt ? HumanReadableUtil.timeSpan(complete_end - decode_end) : complete_end - decode_end + "";
        String before_decode_str = decode_end - channelOpenTime + "";// ����ǰ�˷ѵ�ʱ��
        String decode_str = process_end - decode_end + "";
        String process_str = encode_end - process_end + "";
        String encode_str = complete_end - encode_end + "";

        tmp.append(String.format(timeSpanInfoFmt, stage, all_str, before_decode_str, decode_str, process_str, encode_str));
        return tmp;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void markWriteBegin() {
        this.encode = this.lastWriteTime = System.currentTimeMillis();
    }

    /**
     * ���д����,����������
     */
    public long markWriteEnd() {
        long ori = this.complete;
        this.complete = this.lastWriteTime = System.currentTimeMillis();
        return ori == 0 ? -1 : complete - ori;
    }

    public XLContextAttachment(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
        this.channelOpenTime = this.lastReadTime = this.lastWriteTime = System.currentTimeMillis();// ��ʼ��ʱ��
    }

    public XLHttpRequest getRequest() {// TODO:�˷����ܶ�ط���Ҫ���ã����Ƿ�ֱ���������� checkChannelCLosed
        return request;
    }

    public XLHttpResponse getResponse() {
        return response;
    }

    public long getDecode() {
        return decode;
    }

    public long getProcess() {
        return process;
    }

    public long getEncode() {
        return encode;
    }

    public long getComplete() {
        return complete;
    }

    public CmdMeta getCmdMeta() {
        return cmdMeta;
    }

    public void setCmdMeta(CmdMeta cmdMeta) {
        this.cmdMeta = cmdMeta;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public List<Throwable> getThrowables() {
        return throwables;
    }

    /**
     * ע���µ�resp
     */
    public void registerNewMessage(XLHttpResponse _response) {
        this.response = _response;
        this.process = _response.getCreateTime();
    }

    /**
     * ע���µ�req
     */
    public synchronized boolean registerNewMessage(XLHttpRequest _request) {
        if (this.running) { // pipelining���ڷ�������������Ҫ����attach�ˣ���Ȼ������е����Ӱ��
            log.warn("userAgentTryPipelining:\n{}\n\nANOTHER:{}", getDetailInfo(), _request);
            this.userAgentTryPipelining = true;
        } else { // �������pipelining��ʽ���������ӣ�����Ҫ�ȶ�attach������������ϴ�����Ĳ���
            this.userAgentTryPipelining = false;
            // 2012-06-01 ԭ��û�м���
            this.asyncMessageEventQueueCounter = 0;
            this.asyncMessageEventQueue = null;
            this.response = null;
            // 2012-06-01 ��������������
            // this.channelOpenTime = 0; // ֻ������һ��
            // this.closeAfterOperationComplete = true; // ��Ϊ ����رյĻ�,attach��������Ҳ���˽����ˣ����Բ�������������������
            // 2012-06-01 end

            this.running = true;
            this.request = _request;

            this.decode = _request.getCreateTime();
            this.process = 0;// 2012-06-01�����Ϊʲôԭ��û�м���
            this.encode = 0;
            this.complete = 0;

            this.lastReadTime = this.decode;
            // this.lastWriteTime = ? // 2012-06-01 lastWriteTime �������ã�������

            this.cmdMeta = null;

            this.closeable = null;
            this.throwables = null;
            this.innerAttach = null;

            this.processThread = null;
        }
        return userAgentTryPipelining;
    }

    public Thread getProcessThread() {
        return processThread;
    }

    public synchronized void registerProcessThread() {
        Thread.interrupted();// ���õ�ǰ�̵߳��жϱ�־λ
        this.processThread = Thread.currentThread();
    }

    public synchronized void unregisterProcessThread() {
        this.processThread = null;
    }

    public long getLastReadTime() {
        return lastReadTime;
    }

    public long getLastWriteTime() {
        return lastWriteTime;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        respOperationCompleteCount++;
        running = false;
        if (cmdMeta != null) {// TODO:����Ӧ�ò�����null
            cmdMeta.access(this);
        }
        if (closeAfterOperationComplete) {
            future.getChannel().close();
        }
        if (request != null) {
            request.clean();
        }
    }

    public long getChannelOpenTime() {
        return channelOpenTime;
    }

    public StringBuilder getDetailInfo() {
        return getDetailInfo(new StringBuilder(), System.currentTimeMillis());
    }

    public StringBuilder getDetailInfo(StringBuilder tmp, long now) {
        ChannelHandlerContext ctx = channelHandlerContext;
        if (ctx != null) {// �����ǿ϶��е�
            tmp.append(ctx.getChannel()).append("\n");
        }
        getTimeInfo(tmp, "o:", channelOpenTime);
        getTimeInfo(tmp, "r:", lastReadTime);
        getTimeInfo(tmp, "w:", lastWriteTime);
        tmp.append("respCount:").append(respOperationCompleteCount);
        tmp.append("\n");

        // 1.��ʱ�仨��ʱ��
        tmp.append("TIMESPAN :     ");
        getTimeSpanInfo(tmp, now, true).append(running ? " running" : "").append("\n");

        // 2.�첽��Ϣ
        if (EmptyChecker.isNotEmpty(asyncMessageEventQueue)) {
            tmp.append("ASYNCINFO:\n");
            for (MessageEvent me : asyncMessageEventQueue) {
                tmp.append("\t").append(me).append("\n");
            }
        }

        // 3.���ڴ�����߳�
        if (processThread != null) {
            tmp.append("THREAD   :     ").append(processThread.getName()).append("\n");
        }
        // 4.��;ע��Ŀɹر���Դ
        if (EmptyChecker.isNotEmpty(closeable)) {
            tmp.append("CLOSEABLE:    ");
            for (Object obj : closeable) {
                tmp.append(obj).append(" ");
            }
            tmp.append("\n");
        }

        // 5.��;������쳣
        if (EmptyChecker.isNotEmpty(throwables)) {
            tmp.append("THROWABLE:     ");
            for (Throwable obj : throwables) {
                tmp.append(obj).append(" ");
            }
            tmp.append("\n");
        }

        // 6.����
        if (request != null) {
            // tmp.append("REQUEST  :     \n");
            tmp.append(request.getDetailInfo());
        }

        // 7.��Ӧ
        if (response != null && response.isContentSetted()) {
            tmp.append("RESPONSE :     ").append(response.getStatus()).append("[").append(HumanReadableUtil.byteSize(response.getContentLength())).append("]").append("\n");
            tmp.append(StringHelper.digestString(response.getContentString())).append("\n");
        }
        return tmp;
        // a.getCmdMeta();
        // a.getInnerAttach();
        // a.getLastReadTime();
        // a.getLastWriteTime();
        // a.getThrowables();
    }

    @Override
    public int compareTo(XLContextAttachment o) {
        int r = (int) (channelOpenTime - (o == null ? 0 : o.channelOpenTime));
        if (r == 0) {
            int hash = o == null ? 0 : o.hashCode();
            r = hashCode() - hash;
        }
        return r;
    }

    public boolean setKeepAliveHeader() {
        // int timeout = serverConfig.getKeepAliveTimeout();
        // int responseSettedTimeout = response.getKeepAliveTimeout();
        int timeout = response.getKeepAliveTimeout();
        boolean close = true;
        try {
            // if (response.isKeepAliveTimeoutSetted()) {// A.��ҵ�����Ѿ��������ض���ʱʱ��
            // // timeout = responseSettedTimeout;
            // close = timeout <= 0;
            // } else {
            if (userAgentTryPipelining) {
                close = true;// �����ͼpipeline,��ֻ�����һ���������Ȼ��ر�����
            } else if (timeout == -1) {// B.�ɿͻ���������
                close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)) || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                        && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION));
            } else {// C.ʹ��ȫ������
                close = timeout <= 0;
            }

            // }
        } finally {
            // if (!close)
            // There's no need to add 'Content-Length' header if this is the last response.
            // Ϊ�˼���c����������content-length,�����Ƿ�close������length
            response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(response.getContentLength()));

            if (close) {
                response.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            } else {
                if (timeout != -1) {
                    response.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    response.setHeader(HTTP_HEADER_KEEP_ALIVE, "timeout=" + timeout);
                }
            }
        }
        closeAfterOperationComplete = close;
        return close;
    }
}
