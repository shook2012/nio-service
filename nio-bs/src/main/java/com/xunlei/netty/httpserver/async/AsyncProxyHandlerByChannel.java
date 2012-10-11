package com.xunlei.netty.httpserver.async;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;

public class AsyncProxyHandlerByChannel extends AsyncProxyHandler<Channel> {

    /**
     * <pre>
     * ����ƥ���첽MessageResivedʱ���ܹ��һش�����Channel��Ӧ��Ҫ��Ӧ�� attach,req,resp����Ϣ��
     * 
     * [�ֲ���queue]��ΪmessageResived��ClientBootstrap�еĲ�����n��client worker�߳���ִ�У�����ڲ�ʹ��queue������첽����
     */
    public AsyncProxyHandlerByChannel(ClientBootstrap backstageClientBootstrap, String name, ConcurrentHashMap<SocketAddress, AsyncStat> addressStatMap) {
        super(backstageClientBootstrap, name, addressStatMap);
    }

    public AsyncProxyHandlerByChannel(ClientBootstrap backstageClientBootstrap, String name) {
        super(backstageClientBootstrap, name);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        super.exceptionCaught(ctx, e);

        Channel c = ctx.getChannel();
        AsyncCallbackAttach ca = coreMap.get(c);
        if (ca != null) {
            // �������Զ��server�������Ӳ���ʱ��channel_attach_mapʱ�����һ��queue��û�У��������Ҫ�ų�
            // ˵������ҵ���߳��з����쳣��Ҫ��Ӧ����attach����ProxyClient �������ʱ,Ҳ���� channel.write(msg)�״��ͻ��ܵ������̣�
            exceptionCaught(ctx, e.getCause(), ca);
        }
    }

    @Override
    public Channel getChannel(SocketAddress backstageHostAddress) {
        return newChannel(backstageHostAddress);
    }

    /**
     * <pre>
     * ���յ���̨��������Ϣ��ҵ������ɵĺ�׺����
     * ����� �����ӣ����ǹر�����
     * ����� ʹ�����ӳ����ĳ����ӣ����ǻ�������
     */
    @Override
    protected void messageReceivedFinally(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ctx.getChannel().close();
    }

    @Override
    public AsyncCallbackAttach pollAsyncCallbackAttach(ChannelHandlerContext ctx, MessageEvent e) {
        return coreMap.remove(ctx.getChannel());
    }

    @Override
    public void submit(SocketAddress backstageHostAddress, SequenceMessage msg, AsyncCallbackAttach ca) {
        if (msg == null) {
            return;
        }
        Channel c = getChannel(backstageHostAddress);

        messageSendStat(backstageHostAddress);
        ca.messageSendPrepare(backstageHostAddress, msg);

        coreMap.put(c, ca);
        ca.getAttach().registerCloseable(c);// �������channel�ŵ�closeable�У�����ʱ���������йر�
        c.write(msg);
    }

    // @Override
    // public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {// ˵����ʼ��channel�﷢������
    // XLContextAttachment attach = getAttach(ctx, e);
    // if (attach != null) {// �����ע���������Ϣ������ ��ʱʱ���ر�����
    // attach.registerCloseable(Thread.currentThread());// ��ǰ�̵߳��ɸ����̷߳ŵ�closeable��
    // }
    // super.writeRequested(ctx, e);
    // }

    // public XLContextAttachment getAttach(ChannelHandlerContext ctx, MessageEvent e) {
    // Channel c = ctx.getChannel();
    // Queue<XLContextAttachment> q = channelAttachMap.get(c);
    // // ���������� queue��attach�϶������ǿ�
    // if (q == null) {// ˵��û�� registerChannelAndAttach,Ҳ���� ҵ�������û�� ��ȷ���� getChannel(),�����ⲿֱ�� �½���һ��Channel,Ҳ���Ǵ�Channel���������
    // log.error("cannot find channnel:{} mapped ATTACH QUEUE,reason:[didn't getChannel() properly],message:{}", c, e.getMessage());
    // c.close();
    // return null;
    // }
    // XLContextAttachment attach = q.peek();
    // if (attach == null) {// ˵����ô�Channnel�󣬵���������channel.write(req),��ɺ�̨�����������˶��message
    // log.error("cannot find channnel:{} mapped ATTACH,reason:[getChannel() then send more request,i.e channel.write(req) twice],message:{}", c, e.getMessage());
    // c.close();
    // }
    // return attach;
    // }
}