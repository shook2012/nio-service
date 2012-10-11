package com.xunlei.netty.httpserver.async;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import com.xunlei.netty.httpserver.async.pool.SequenceChannelPool;
import com.xunlei.util.Log;

/**
 * @author ZengDong
 * @since 2012-3-22 ����1:01:39
 */
public class SyncProxyHandlerBySequence extends XLClientHandler {

    private static final Logger log = Log.getLogger();
    protected SequenceChannelPool channelPool;

    private static class SeqReq {

        protected SequenceMessage req;
        protected long messageSendTime;

        public SeqReq(SequenceMessage req, long messageSendTime) {
            this.req = req;
            this.messageSendTime = messageSendTime;
        }
    }

    protected final Map<Long, SeqReq> reqPool = new ConcurrentHashMap<Long, SeqReq>();
    protected final Map<Long, SequenceMessage> respPool = new ConcurrentHashMap<Long, SequenceMessage>();

    public SyncProxyHandlerBySequence(ClientBootstrap backstageClientBootstrap, String name) {
        super(backstageClientBootstrap, name);
        this.channelPool = new SequenceChannelPool(this);
    }

    public SyncProxyHandlerBySequence(ClientBootstrap backstageClientBootstrap, String name, ConcurrentHashMap<SocketAddress, AsyncStat> addressStatMap, SequenceChannelPool channelPool) {
        super(backstageClientBootstrap, name);
        this.channelPool = channelPool;
    }

    @Override
    public Channel getChannel(SocketAddress backstageHostAddress) {
        return channelPool.getChannel(backstageHostAddress);
    }

    protected String getNotifyLock(SequenceMessage msg) {
        return ("SyncProxyHandlerBySequence:" + msg.getSequence()).intern();
        // ��һ�ַ����� ֱ��Lock req
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        super.messageReceived(ctx, e);
        SequenceMessage msg = (SequenceMessage) e.getMessage();
        Long seq = msg.getSequence();
        SeqReq req = reqPool.remove(seq);
        if (req != null) { // ˵����û�г�ʱ
            messageRecvStat(ctx.getChannel().getRemoteAddress(), req.messageSendTime, req.req);
            respPool.put(msg.getSequence(), msg);
            // notify(req); //��ʵֻҪ���������Ҫnotify�����������������[��req��lock�Ļ����ͱ�������д]
        } else {// ˵���ǳ�ʱ��
            log.error("reqPool not found[timeout],resp:{}", msg);
        }
        notify(msg);
    }

    protected void notify(SequenceMessage msg) {
        String lock = getNotifyLock(msg);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    private void printReqPool(StringBuilder sb, String na, Map<Long, SeqReq> pool) {
        sb.append(na).append("[").append(pool.size()).append("]:").append("\n");
        for (SeqReq sr : pool.values()) {
            SequenceMessage msg = sr.req;
            sb.append(msg.getSequence()).append("\t").append(msg).append("\n");
        }
        sb.append("\n\n");
    }

    private void printPool(StringBuilder sb, String na, Map<Long, SequenceMessage> pool) {
        sb.append(na).append("[").append(pool.size()).append("]:").append("\n");
        for (SequenceMessage msg : pool.values()) {
            sb.append(msg.getSequence()).append("\t").append(msg).append("\n");
        }
        sb.append("\n\n");
    }

    @Override
    public StringBuilder printStatInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(channelPool);
        sb.append("\n----------------------------------------------------\n");
        printReqPool(sb, "reqPool", reqPool);// reqPool����� �����ڵȴ��ذ�����Ϣ��������Ƚ϶࣬˵�����������
        printPool(sb, "respPool", respPool);// respPool�϶�Ҫ�Ǻ�С���У��������� 0��1
        sb.append("\n----------------------------------------------------\n");
        sb.append(super.printStatInfo());
        return sb;
    }

    @SuppressWarnings("unchecked")
    public <T extends SequenceMessage> T send(SocketAddress backstageHostAddress, SequenceMessage msg, long timeout) {
        T r = null;
        if (msg == null) {
            return r;
        }
        long seq = msg.getSequence();
        Channel c = getChannel(backstageHostAddress);

        long b = System.currentTimeMillis();
        SeqReq sr = new SeqReq(msg, b);

        messageSendStat(backstageHostAddress);

        reqPool.put(seq, sr);
        try {
            c.write(msg);// �����첽����ע�ⲻ�� �ŵ� synchoronized(lock) ��Ȼ������

            r = (T) respPool.remove(seq);// ������ȡһ�Σ��п��ܲ��õȾ����õ���[�����ϲ�����]
            if (r == null) {
                wait(msg, timeout);
                r = (T) respPool.remove(seq);
            }
        } finally {
            SeqReq reqMsg = reqPool.remove(seq);

            if (reqMsg != null && r == null) {
                getAsyncStat(backstageHostAddress).timeoutCounter.incrementAndGet(); // ���������ܸ����ͳ�Ƶ� ��ʱ��

                if (r == null) {// ���õ�before˵���ǳ�ʱ�ˣ���ʱ�϶��� r == null
                    log.error("send req:{},get resp timeout/interrupt:{}ms", msg, System.currentTimeMillis() - b);
                }
            }
        }
        return r;
    }

    protected void wait(SequenceMessage msg, long timeout) {
        String lock = getNotifyLock(msg);
        synchronized (lock) {
            try {
                lock.wait(timeout);
            } catch (InterruptedException e) {
                log.error("wait resp encount InterruptedException,req:{}", msg);
            }
        }
    }
}
