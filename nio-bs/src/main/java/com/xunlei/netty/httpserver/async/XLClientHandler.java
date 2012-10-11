package com.xunlei.netty.httpserver.async;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import com.xunlei.netty.httpserver.async.AsyncProxyHandler.AsyncCallbackAttach;
import com.xunlei.netty.httpserver.util.NetUtil;
import com.xunlei.util.Log;

public abstract class XLClientHandler extends SimpleChannelHandler {

    protected final Logger log = Log.getLogger(this);
    protected String name;
    /** ��ͬaddrͳ����Ϣ */
    protected Map<SocketAddress, AsyncStat> addressStatMap;
    /** ��������Client */
    protected ClientBootstrap backstageClientBootstrap;

    public XLClientHandler(ClientBootstrap backstageClientBootstrap, String name) {
        this.name = name;
        this.backstageClientBootstrap = backstageClientBootstrap;
        this.addressStatMap = new ConcurrentHashMap<SocketAddress, AsyncStat>();
    }

    public XLClientHandler(ClientBootstrap backstageClientBootstrap, String name, ConcurrentHashMap<SocketAddress, AsyncStat> addressStatMap) {
        this.name = name;
        this.addressStatMap = addressStatMap;
        this.backstageClientBootstrap = backstageClientBootstrap;
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // super.channelClosed(ctx, e);
        log.info("[{}]proxy channel is closed:{}", name, ctx.getChannel());
        getAsyncStat(ctx.getChannel().getRemoteAddress()).channelCloseCounter.incrementAndGet();
        // if (ctx.getChannel().getRemoteAddress() != null) {// ���һ��ʼ�����Ӳ���,�����ò���addr
        // getClientAttach(ctx).asyncStat.channelCloseCounter.incrementAndGet();
        // }
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        // �����½�����ʱֱ��ʹ�ã������Ƿŵ����ӳ��� // channelPool.offer(ctx.getChannel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        // ���ﲶ�� channel���Ӽ���������쳣����Щ�ؼ����������ʱ��Ϊ�˱��������ֱ�ӹرմ�channel
        // NetUtil.exceptionCaught(ctx, e, "asyncProxyHandler"); // 2011-12-16 �������������Ϊ�ڲ���ֱ�ӹر�channel

        // 2011-12-20 ����������ˣ���Ϊ����û�йر�channel,�������ڵ��߼�������Ӧ���ܵ��˴�
        NetUtil.exceptionCaught(ctx, e, name);

        // Channel channel = e.getChannel();
        // Object attach = ctx.getAttachment();
        // Object v = attach == null ? channel : attach;// �����attach,�ʹ�ӡattach
        // log.error("exceptionCaught in async proxy:{}", new Object[] { v, e.getCause() });
    }

    public AsyncStat getAsyncStat(SocketAddress addr) {
        AsyncStat as = addressStatMap.get(addr);
        if (as == null) {
            synchronized (addressStatMap) {
                as = addressStatMap.get(addr);
                if (as == null) {
                    as = new AsyncStat(addr);
                    addressStatMap.put(addr, as);
                }
            }
        }
        return as;
    }

    public void messageSendStat(SocketAddress addr) {// ͳ�Ʒ���
        getAsyncStat(addr).requestCounter.incrementAndGet();
    }

    public void messageRecvStat(SocketAddress addr, AsyncCallbackAttach attach) { // ͳ���հ�
        AsyncStat stat = getAsyncStat(addr);
        stat.responseCounter.incrementAndGet();
        stat.asyncClientStat.record(System.currentTimeMillis(), attach.getMessageSendTime(), attach);
    }

    public void messageRecvStat(SocketAddress addr, long messageSendTime, Object recordInfo) { // ͳ���հ�,����ͬ������
        AsyncStat stat = getAsyncStat(addr);
        stat.responseCounter.incrementAndGet();
        stat.asyncClientStat.record(System.currentTimeMillis(), messageSendTime, recordInfo);
    }

    public abstract Channel getChannel(SocketAddress backstageHostAddress);

    static { // 2012-6-14 netty����channel�Ĺ������첽�ģ��ٷ�����await*����Ӧ����������߳�������ã������ｨ�����ǳ����ӣ�ֻ������ʱ�������ӵģ�û�б�Ҫ���첽���ɣ��ʴ˴��Ѽ��ر�
        DefaultChannelFuture.setUseDeadLockChecker(false);
    }

    public Channel newChannel(SocketAddress backstageHostAddress) {
        ChannelFuture cf = backstageClientBootstrap.connect(backstageHostAddress);
        Channel c = cf.awaitUninterruptibly().getChannel();// ����ͬ���ȴ�
        AsyncStat as = getAsyncStat(backstageHostAddress);
        if (!c.isConnected()) { // ��鵱ǰchannel����Ч��
            int count = as.newChannelFailCounter.incrementAndGet();
            log.error("[{}]cannot creat a channel:{},historyCount:{}", new Object[] { name, backstageHostAddress, count });
            throw new RuntimeException("[" + name + "]cannt creat a channel:" + backstageHostAddress + ",historyCount:" + count);
        }
        int count = as.newChannelOkCounter.incrementAndGet();
        log.debug("[{}]create a channel:{},historyCount:{}", new Object[] { name, c, count });
        return c;
    }

    // public Channel newChannel(final SocketAddress backstageHostAddress) {
    // ChannelFuture cf = backstageClientBootstrap.connect(backstageHostAddress);
    // final Channel[] c = new Channel[1];
    // cf.addListener(new ChannelFutureListener() {
    //
    // @Override
    // public void operationComplete(ChannelFuture cf1) throws Exception {
    // Channel ch = cf1.awaitUninterruptibly().getChannel();// ����ͬ���ȴ�
    // AsyncStat as = getAsyncStat(backstageHostAddress);
    // if (!ch.isConnected()) { // ��鵱ǰchannel����Ч��
    // int count = as.newChannelFailCounter.incrementAndGet();
    // log.error("[{}]cannot creat a channel:{},historyCount:{}", new Object[] { name, backstageHostAddress, count });
    // throw new RuntimeException("[" + name + "]cannt creat a channel:" + backstageHostAddress + ",historyCount:" + count);
    // }
    // int count = as.newChannelOkCounter.incrementAndGet();
    // log.debug("[{}]create a channel:{},historyCount:{}", new Object[] { name, c, count });
    // c[0] = ch;
    // }
    // });
    // while (null == c[0]) {
    // }
    // return c[0];
    // }

    // @Override
    // public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
    // // ���������ɺ󣬻�Ҫ�ȴ�realServer��Ӧ- MessageResived���ݲ��Żص����ӳ���// channelPool.offer(ctx.getChannel());
    // // ���´�������ͳ����
    // getClientAttach(ctx).writeComplete(); // 2012-04-18 �����һ��ʼ��ԭʼ�뷨����ʵ�Ǵ�ģ�writeComplete������Ϊ���� ��һ��������������
    // }

    // @Override
    // public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {// 2012-04-18 �����һ��ʼ��ԭʼ�뷨����ʵ�Ǵ�ģ������Ѿ�����ҪXLClientContextAttachment��
    // // ͳ�ƽ��յ���Զ�̵���Ӧ
    // Channel c = ctx.getChannel();
    // XLClientContextAttachment clientAttach = (XLClientContextAttachment) ctx.getAttachment();
    // if (clientAttach == null) {
    // log.error("cannot find clientAttach when messageReceived,channel:{},message:{}", c, e.getMessage());
    // } else {
    // clientAttach.messageReceived(e);
    // }
    // }

    // /**
    // * ��ctx�л��clientAttach��û�����½�һ��
    // */
    // public XLClientContextAttachment getClientAttach(ChannelHandlerContext ctx) {
    // XLClientContextAttachment clientAttach = (XLClientContextAttachment) ctx.getAttachment();
    // if (clientAttach == null) {
    // SocketAddress addr = ctx.getChannel().getRemoteAddress();
    // XLClientContextAttachment newAttach = new XLClientContextAttachment(ctx, getAsyncStat(addr));
    // ctx.setAttachment(newAttach);
    // return newAttach;
    // }
    // return clientAttach;
    // }

    /**
     * ���ͳ����Ϣ
     */
    public StringBuilder printStatInfo() {
        StringBuilder sb = new StringBuilder();
        for (AsyncStat as : addressStatMap.values()) {
            sb.append(as).append("\n\n");
        }
        return sb;
    }
}
