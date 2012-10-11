package com.xunlei.netty.httpserver.component;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import com.xunlei.netty.httpserver.cmd.CmdMappers;
import com.xunlei.netty.httpserver.util.HttpServerConfig;
import com.xunlei.netty.httpserver.util.NetUtil;

/**
 * ����ַ�����һ�㴦��
 * 
 * <pre>
 * netty��bindһ���˿ھ���һ�� boss�߳�һֱ�ڵȴ��µ�����(NioServerSocketPipelineSink),�����µ�������ʱ(registerAcceptedChannel),����nextWorker
 * ��� NioWorker.register(channel)�������������,Ȼ���ڲ���worker�̳߳�������ҵ����
 * 
 * �߳����У�New I/O server worker #1-4,����1��ʾbossid,4��ʾworkerid
 * 
 * ���Է��֣����worker����һֱ����(ռ����ͨ��ʱ),�����µ�����ʱ,boss�������䲻������
 * 
 * channelOpen -- channelBound --channelConnected
 * channelDisconnected -- channelUnbound -- channelClosed
 * disconnected ˵���Լ��Ѿ���ʼ�����ر������ˣ�Ӧ���ǽ���time_wait״̬
 * channelClosed ˵�������������ͷ�������
 * 
 * time_wait����
 * http://wenku.baidu.com/view/72333049e45c3b3567ec8b65.html
 * 
 * net.ipv4.tcp_tw_reuse = 1
 * net.ipv4.tcp_tw_recycle = 1
 * net.ipv4.tcp_fin_timeout = 30
 * net.ipv4.tcp_max_tw_buckets = 1000
 * </pre>
 * 
 * <pre>
 * ����ע�⣬��pipelineǰ���̳߳ر����� OMAT ����ģ�ԭ���ǣ��ڴ����в�ͬ���¼������ж�˳���ر����У�
 *    ��channelOpen ���½�һ��Attach,messageReceived��Դ�Attach�����ٴ���������̳߳ظĳ�����Ļ������кܶ����󱨴�
 *    
 * 17:38:46|.httpserver.util.NetUtil.exceptionCaught|ERROR exceptionCaught   :[id: 0x4ccf31e5, /119.145.40.161:3157 => /61.147.81.107:80] (NetUtil.java:105) PIPELINE#2 
 * java.lang.NullPointerException: null
 *     at com.xunlei.netty.httpserver.component.BasePageDispatcher.messageReceived(BasePageDispatcher.java:107) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.execution.ChannelUpstreamEventRunnable.run(ChannelUpstreamEventRunnable.java:44) [netty-3.4.6.Final.jar:na]
 *     at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886) [na:1.6.0_32]
 *     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908) [na:1.6.0_32]
 *     at java.lang.Thread.run(Thread.java:662) [na:1.6.0_32]
 *     
 *     
 *  ���� messageReceivedҲ��Chunk�������
 *  17:38:06|.httpserver.util.NetUtil.exceptionCaught|ERROR exceptionCaught   :null-d10f642/119.145.40.161:48615/decode:993(10|993,0,0) (NetUtil.java:105) PIPELINE#66 
 * java.lang.IndexOutOfBoundsException: null
 *     at org.jboss.netty.buffer.AbstractChannelBuffer.readerIndex(AbstractChannelBuffer.java:44) ~[netty-3.4.6.Final.jar:na]
 *     at org.jboss.netty.handler.codec.http.HttpPostRequestDecoder.readFileUploadByteMultipart(HttpPostRequestDecoder.java:1153) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.codec.http.HttpPostRequestDecoder.getFileUpload(HttpPostRequestDecoder.java:993) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.codec.http.HttpPostRequestDecoder.decodeMultipart(HttpPostRequestDecoder.java:687) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.codec.http.HttpPostRequestDecoder.parseBodyMultipart(HttpPostRequestDecoder.java:589) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.codec.http.HttpPostRequestDecoder.parseBody(HttpPostRequestDecoder.java:419) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.codec.http.HttpPostRequestDecoder.offer(HttpPostRequestDecoder.java:369) ~[xl-ruiz-1.0.jar:na]
 *     at com.xunlei.netty.httpserver.component.XLHttpRequest.offerChunk(XLHttpRequest.java:533) ~[xl-ruiz-1.0.jar:na]
 *     at com.xunlei.netty.httpserver.component.BasePageDispatcher.messageReceived(BasePageDispatcher.java:123) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.execution.ChannelUpstreamEventRunnable.run(ChannelUpstreamEventRunnable.java:44) [netty-3.4.6.Final.jar:na]
 *     at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886) [na:1.6.0_32]
 *     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908) [na:1.6.0_32]
 *     at java.lang.Thread.run(Thread.java:662) [na:1.6.0_32]
 * 17:38:06|.httpserver.util.NetUtil.exceptionCaught|ERROR exceptionCaught   :null-d10f642/119.145.40.161:48615/decode:993(10|993,0,0) (NetUtil.java:105) PIPELINE#1 
 * java.lang.NullPointerException: null
 *     at org.jboss.netty.handler.codec.http.AbstractDiskHttpData.addContent(AbstractDiskHttpData.java:159) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.codec.http.MixedFileUpload.addContent(MixedFileUpload.java:70) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.codec.http.HttpPostRequestDecoder.readFileUploadByteMultipart(HttpPostRequestDecoder.java:1151) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.codec.http.HttpPostRequestDecoder.getFileUpload(HttpPostRequestDecoder.java:993) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.codec.http.HttpPostRequestDecoder.decodeMultipart(HttpPostRequestDecoder.java:687) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.codec.http.HttpPostRequestDecoder.parseBodyMultipart(HttpPostRequestDecoder.java:589) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.codec.http.HttpPostRequestDecoder.parseBody(HttpPostRequestDecoder.java:419) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.codec.http.HttpPostRequestDecoder.offer(HttpPostRequestDecoder.java:369) ~[xl-ruiz-1.0.jar:na]
 *     at com.xunlei.netty.httpserver.component.XLHttpRequest.offerChunk(XLHttpRequest.java:533) ~[xl-ruiz-1.0.jar:na]
 *     at com.xunlei.netty.httpserver.component.BasePageDispatcher.messageReceived(BasePageDispatcher.java:123) ~[xl-ruiz-1.0.jar:na]
 *     at org.jboss.netty.handler.execution.ChannelUpstreamEventRunnable.run(ChannelUpstreamEventRunnable.java:44) [netty-3.4.6.Final.jar:na]
 *     at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886) [na:1.6.0_32]
 *     at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908) [na:1.6.0_32]
 *     at java.lang.Thread.run(Thread.java:662) [na:1.6.0_32]
 * </pre>
 * 
 * @author ZengDong
 * @since 2010-3-18 ����01:41:21
 */
public abstract class BasePageDispatcher extends AbstractPageDispatcher {

    // private static final Logger log = Log.getLogger();
    @Autowired
    protected CmdMappers cmdMappers;
    @Autowired
    protected HttpServerConfig config;

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
        config.getStatistics().channelClosed(ctx, e);

        XLContextAttachment attach = (XLContextAttachment) ctx.getAttachment();
        timeoutInterrupter.getAttachRegister().unregisterAttach(attach);

        if (attach.interrupt(null)) {
            config.getStatistics().channelInterruptClosed(ctx);
        }
        XLHttpRequest request = attach.getRequest();// ������ response writeCompleteʱ��Ҫclean���ڶϿ�����ʱҲ��Ҫ(��Щ������û�Ȼذ��ͶϿ�������)
        if (request != null) {
            request.clean();
        }
    }

    @Autowired
    private TimeoutInterrupter timeoutInterrupter;

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        ctx.sendUpstream(e);
        config.getStatistics().channelOpen(ctx, e);

        XLContextAttachment attach = getAttach(ctx);
        ctx.setAttachment(attach);// ���ø��Ŷ���
        timeoutInterrupter.getAttachRegister().registerAttach(attach);
    }

    protected abstract void dispatch(XLContextAttachment attach) throws Exception;

    /**
     * <pre>
     * ��û�б�ǰ���messageReceived�е�catch Throwable��throwableHandlerManager�ػ���������,����ŵ��˷���
     * 
     * Ҳ�������ﲻ���� ҵ���쳣
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        NetUtil.exceptionCaught(ctx, e);
        e.getChannel().close();
    }

    @Override
    public void init() {
    }

    // 2012-05-31 Ϊ��ʹ�ô�����Բ��� OrderMemoryAwareExecutor�������� MemoryAwareExecutor,���ܱ�֤ �¼�˳�������£�attach��Ҫ���������(channelOpen��messageReceivedt���¼���ʱ�����������)
    public XLContextAttachment getAttach(ChannelHandlerContext ctx) {
        XLContextAttachment attach = (XLContextAttachment) ctx.getAttachment();
        if (attach == null) {
            synchronized (ctx) {
                attach = (XLContextAttachment) ctx.getAttachment();
                if (attach == null) {
                    attach = new XLContextAttachment(ctx);
                    ctx.setAttachment(attach);// ���ø��Ŷ���
                }
            }
        }
        return attach;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent messageEvent) throws Exception {
        XLContextAttachment attach = getAttach(ctx);
        Object obj = messageEvent.getMessage();
        try {
            if (obj instanceof XLHttpRequest) {
                XLHttpRequest request = (XLHttpRequest) obj;
                request.setRemoteAddress(messageEvent.getRemoteAddress());
                request.setLocalAddress(messageEvent.getChannel().getLocalAddress());
                boolean userAgentTryPipelining = attach.registerNewMessage(request);
                if (userAgentTryPipelining) {
                    // ���Դ�����
                    return;
                }

                if (request.isChunked()) {// ˵�������ϴ��ļ�
                    // TODO:�����ǲ���Ҫ�����ϴ��ļ���С
                } else {
                    requestReceived(ctx, attach, request, messageEvent);
                }
            }

            // 2012-05-31 ��HttpChunk�ľۺϲ��˷ŵ����࣬������ҵ�����࣬Ϊ�����ܿ��ǻ����̳߳������ܣ��������ó� �¼�˳�����еģ�����OrderMemoryAwareExecutor��
            // else if (obj instanceof HttpChunk) {
            //
            // XLHttpRequest request = attach.getRequest();
            // HttpChunk httpChunk = (HttpChunk) obj;
            //
            // request.offerChunk(httpChunk);
            //
            // attach.markLastReadTime();
            //
            // if (httpChunk.isLast()) {
            // requestReceived(ctx, attach, request, messageEvent);
            // }
            // }

            else {
                throw new RuntimeException("cant resolve message:" + obj);
            }
        } finally {
            ctx.sendUpstream(messageEvent);// ȷ������handler������
        }
    }

    private void requestReceived(ChannelHandlerContext ctx, XLContextAttachment attach, XLHttpRequest request, MessageEvent messageEvent) throws Exception {
        XLHttpResponse response = new XLHttpResponse(attach);
        attach.registerNewMessage(response);

        config.getStatistics().messageReceived(attach);
        // try {
        dispatch(attach);
        // } finally {
        // ctx.sendUpstream(messageEvent);// ȷ������handler������
        // }
    }

    @Override
    public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
        // ����һ����Ӧ���ܻ�ֲ�ɶ��������
        XLContextAttachment attach = getAttach(ctx);
        // if (attach != null) //������
        config.getStatistics().writeComplete(attach, e);
        ctx.sendUpstream(e);
    }

}
