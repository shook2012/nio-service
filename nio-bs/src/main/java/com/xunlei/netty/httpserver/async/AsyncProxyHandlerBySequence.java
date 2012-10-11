package com.xunlei.netty.httpserver.async;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import com.xunlei.netty.httpserver.async.pool.SequenceChannelPool;

public class AsyncProxyHandlerBySequence extends AsyncProxyHandler<Long> {

    protected SequenceChannelPool channelPool;

    public AsyncProxyHandlerBySequence(ClientBootstrap backstageClientBootstrap, String name) {
        super(backstageClientBootstrap, name);
        this.channelPool = new SequenceChannelPool(this);
    }

    public AsyncProxyHandlerBySequence(ClientBootstrap backstageClientBootstrap, String name, ConcurrentHashMap<SocketAddress, AsyncStat> addressStatMap, SequenceChannelPool channelPool) {
        super(backstageClientBootstrap, name, addressStatMap);
        this.channelPool = channelPool;
    }

    // @Override
    // public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    // // ���ﲶ�� channel���Ӽ���������쳣����Щ�ؼ����������ʱ��Ϊ�˱��������ֱ�ӹرմ�channel
    // NetUtil.exceptionCaught(ctx, e);
    // // ��Ϊ�������Ҳ��� seq������Ҳ�ز���
    // }

    @Override
    public Channel getChannel(SocketAddress backstageHostAddress) {
        return channelPool.getChannel(backstageHostAddress);
    }

    @Override
    public AsyncCallbackAttach pollAsyncCallbackAttach(ChannelHandlerContext ctx, MessageEvent e) {
        SequenceMessage msg = (SequenceMessage) e.getMessage();
        long seq = msg.getSequence();
        return coreMap.remove(seq);
    }

    @Override
    public StringBuilder printStatInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(channelPool);
        sb.append("\n----------------------------------------------------\n");
        sb.append(printStatInfo(coreMap));
        return sb;
    }

    @Override
    public void submit(SocketAddress backstageHostAddress, SequenceMessage msg, AsyncCallbackAttach ca) {
        if (msg == null) {
            return;
        }
        Channel c = getChannel(backstageHostAddress);

        messageSendStat(backstageHostAddress);
        ca.messageSendPrepare(backstageHostAddress, msg);

        coreMap.put(msg.getSequence(), ca);
        // ca.getAttach().registerCloseable(c);// �������channel�ŵ�closeable�У�����ʱ���������йر�
        // 2011-12-11 ��Ϊ��������ȫ˫���ظ������ӣ����Բ����ڳ�ʱ��ʱ��ر�
        c.write(msg);
    }

    // @Override
    // public String toString() {
    // return "AsyncProxyHandlerBySequence [channelPool=" + channelPool + ", channelPoolSize=" + channelPoolSize + ", counter=" + counter + "]";
    // }
}