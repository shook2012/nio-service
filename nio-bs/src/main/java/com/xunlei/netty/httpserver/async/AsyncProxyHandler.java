package com.xunlei.netty.httpserver.async;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import com.xunlei.netty.httpserver.component.XLContextAttachment;
import com.xunlei.netty.httpserver.exception.ClosedChannelError;
import com.xunlei.netty.httpserver.exception.ProcessFinishedError;
import com.xunlei.netty.httpserver.handler.TextResponseHandlerManager;
import com.xunlei.util.DateStringUtil;
import com.xunlei.util.HumanReadableUtil;
import com.xunlei.util.Log;
import com.xunlei.util.concurrent.BaseSchedulable;

/**
 * <pre>
 * AsyncProxyHandler �첽Proxy������
 * һ���÷���
 *   1.httpServer��cmd ҵ��� �������Channel����������Message
 *     asyncHandler.write(response.getAttach(),asyncCallback,actualServerAddr,message);
 * 
 *   2.asyncCallback ���첽�����Ϣ��Ļص��ӿ�
 *     messageReceived(ChannelHandlerContext ctx, MessageEvent e, XLContextAttachment attach)
 *     exceptionCaught(ChannelHandlerContext ctx, ChannelEvent e, XLContextAttachment attach)
 * 
 *     �����Ҫ����д�ص�httpServer�������ڴ��������ϵ���writeResponseToFront()
 * 
 * 
 * ͨ��client�˵����Ӷ������ز�callback,attachӳ��:(��˫��)
 * AsyncProxyHandlerByChannelOneAddr       ��ʼ��ʱ��ָ������actualServerAddr��Ψһ��ַ
 * AsyncProxyHandlerByChannelOneAddrPooled ��ʼ��ʱ��ָ������actualServerAddr��Ψһ��ַ,��ʹ���˳����Ӽ����ӳ�
 * AsyncProxyHandlerByChannelPooled        ʹ���˳����Ӽ����ӳأ������������ӹ���Channel
 * 
 * ͨ�������ذ��е�sequence�ֶ����ز�callback,attachӳ��:(ȫ˫��)
 * AsyncProxyHandlerBySequence
 * 
 * @author ZengDong
 * @param <T>
 * @since 2011-10-7 ����04:03:02
 */
public abstract class AsyncProxyHandler<T> extends XLClientHandler {

    public static class AsyncCallbackAttach {

        protected AsyncCallback _callback;
        protected XLContextAttachment _attach;

        protected List<SequenceMessage> _messageList;
        protected List<SocketAddress> _addressList;
        protected long _messageSendTime;// ������Ϣʱ��

        protected AsyncCallbackAttach(XLContextAttachment attach) {
            this._attach = attach;
        }

        public AsyncCallbackAttach(XLContextAttachment attach, AsyncCallback callback) {
            this._callback = callback;
            this._attach = attach;
        }

        public AsyncCallback getCallback() {
            return _callback;
        }

        public XLContextAttachment getAttach() {
            return _attach;
        }

        public void messageSendPrepare(SocketAddress addr, SequenceMessage message) {
            if (this._messageList == null) {
                _messageList = new ArrayList<SequenceMessage>(1); // Ĭ����Ϊ���� ��һ������
                _addressList = new ArrayList<SocketAddress>(1);
                _messageSendTime = System.currentTimeMillis();
            }
            _messageList.add(message);
            _addressList.add(addr);
        }

        public SocketAddress getAddressBySeq(long seq) {
            if (this._messageList != null) {
                for (int i = 0; i < _messageList.size(); i++) {
                    SequenceMessage msg = _messageList.get(i);
                    if (msg.getSequence() == seq) {
                        return _addressList.get(i);
                    }
                }
            }
            return null;
        }

        public long getMessageSendTime() {
            return _messageSendTime;
        }
    }

    public static NothingCallbackAttach NOTHING_CALLBACK_ATTACH = new NothingCallbackAttach();

    private static class NothingCallbackAttach extends AsyncCallbackAttach implements AsyncCallback {

        private static final Logger log = Log.getLogger();

        private NothingCallbackAttach() {
            super(new XLContextAttachment(null) {

                @Override
                public void initAsyncMessageEventQueue(int concurrentNum) {
                }

                @Override
                public void registerCloseable(Object obj) {
                }

                @Override
                public void registerCloseable(Thread t) {
                }

                @Override
                public void checkChannelOrThread() {
                }

                @Override
                public synchronized void unregisterCloseable(Object obj) {
                }

                @Override
                public synchronized void unregisterProcessThread() {
                }

                @Override
                public String getName() {
                    return "NothingCallbackAttach";
                }
            });
        }

        @Override
        public AsyncCallback getCallback() {
            return this;
        }

        @Override
        public void messageSendPrepare(SocketAddress addr, SequenceMessage message) {
            // nothing
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e, XLContextAttachment attach) throws Exception {
            // nothingCallBack
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable e, XLContextAttachment attach) throws Exception {// TODO:
            log.error("ctx:{}", ctx, e);
        }
    }

    private static final Logger logger = Log.getLogger();
    public static final Object ASYNC_RESPONSE = new Object();// ���ڱ�ʶ��ǰ������첽��Ӧ
    protected final Map<T, AsyncCallbackAttach> coreMap = new ConcurrentHashMap<T, AsyncCallbackAttach>();
    private long coreMapPurgeCount = 0;
    private String coreMapLastPurgeInfo = "";
    private DateStringUtil dsu_time = DateStringUtil.getInstance("HH:mm:ss");

    private void trimTimeoutAttach(long timeout) {
        StringBuilder info = new StringBuilder();
        int ori = coreMap.size();
        int count = 0;
        for (Map.Entry<T, AsyncCallbackAttach> e : coreMap.entrySet()) {
            AsyncCallbackAttach ac = e.getValue();
            T key = e.getKey();
            long span = System.currentTimeMillis() - ac.getMessageSendTime();
            if (span > timeout) {
                coreMap.remove(key);
                count++;
                coreMapPurgeCount++;

                SocketAddress addr = null;
                if (key instanceof Long) { // ����Ϊ������ٶ��ҵ� Addr��������д
                    addr = ac.getAddressBySeq((Long) key);
                } else if (key instanceof Channel) {
                    addr = ((Channel) key).getRemoteAddress();
                }

                if (addr != null) {
                    AsyncStat as = getAsyncStat(addr);
                    as.timeoutCounter.incrementAndGet(); // ��¼��stat��ʱ
                    // as.asyncClientStat.record(System.currentTimeMillis(), ac.getMessageSendTime(), ac); // ����¼������
                    info.append(HumanReadableUtil.timeSpan(span)).append("\t").append(key).append("->").append(addr).append("\t").append(ac);
                } else {
                    info.append(HumanReadableUtil.timeSpan(span)).append("\t").append(key).append("->NA").append("\t").append(ac);
                }
                info.append("\n");
            }
        }
        int now = coreMap.size();

        if (now > coreMapWarnSize) {
            coreMapLastPurgeInfo = "\tpurgeCount:[" + coreMapPurgeCount + "]\tlastPurge:[" + dsu_time.now() + "]";
            log.error("!!WARNING!! -- count:{},ori:{}->now:{},purgeCount:{},timeout:{},detail:\n{}", new Object[] { count, ori, now, coreMapPurgeCount, timeout, info });
        } else if (count > 0) {
            coreMapLastPurgeInfo = "\tpurgeCount:[" + coreMapPurgeCount + "]\tlastPurge:[" + dsu_time.now() + "]";
            log.debug("count:{},ori:{}->now:{},purgeCount:{},timeout:{},detail:\n{}", new Object[] { count, ori, now, coreMapPurgeCount, timeout, info });
        }
    }

    private long attachTimeoutSec = 3 * 60;// Ĭ��3���Ӿͳ�ʱ
    private long attachCheckSec = 60;// Ĭ��1������ѯһ��
    private int coreMapWarnSize = 5000;// 5000����˵��Ҫ����
    private BaseSchedulable baseSchedulable = new BaseSchedulable() {

        @Override
        public void process() throws Throwable {
            trimTimeoutAttach(attachTimeoutSec * 1000);
        }
    };

    public long getAttachTimeoutSec() {
        return attachTimeoutSec;
    }

    public void setAttachTimeoutSec(long attachTimeoutSec) {
        this.attachTimeoutSec = attachTimeoutSec;
    }

    public long getAttachCheckSec() {
        return attachCheckSec;
    }

    public void setAttachCheckSec(long attachCheckSec) {
        this.attachCheckSec = attachCheckSec;
        baseSchedulable.scheduleWithFixedDelaySec(attachCheckSec);
    }

    public int getCoreMapWarnSize() {
        return coreMapWarnSize;
    }

    public void setCoreMapWarnSize(int coreMapWarnSize) {
        this.coreMapWarnSize = coreMapWarnSize;
    }

    /**
     * ��д��Ӧ��ǰ��
     */
    public static void writeResponseToFront(TextResponseHandlerManager localHttpServerResponseHandlerManager, XLContextAttachment attach, Object cmdReturnObj) {
        try {
            localHttpServerResponseHandlerManager.writeResponse(attach, cmdReturnObj);
        } catch (ClosedChannelError e) {
            logger.error("channelClosed    :{}", attach.getChannelHandlerContext().getChannel());
        }
    }

    public AsyncProxyHandler(ClientBootstrap backstageClientBootstrap, String name, ConcurrentHashMap<SocketAddress, AsyncStat> addressStatMap) {
        super(backstageClientBootstrap, name, addressStatMap);
        setAttachCheckSec(attachCheckSec);// ������ʱ����
    }

    public AsyncProxyHandler(ClientBootstrap backstageClientBootstrap, String name) {
        super(backstageClientBootstrap, name);
        setAttachCheckSec(attachCheckSec);// ������ʱ����
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e, AsyncCallbackAttach ca) throws Exception {
        ca.getCallback().exceptionCaught(ctx, e, ca.getAttach());
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        super.messageReceived(ctx, e);
        Channel c = ctx.getChannel();
        AsyncCallbackAttach ca = pollAsyncCallbackAttach(ctx, e);
        XLContextAttachment attach = null;
        if (ca == null) {
            log.error("cannot find AsyncCallbackAttach when messageReceived,client_channel:{},message:{}", c, e.getMessage());
            return;
        }
        messageRecvStat(c.getRemoteAddress(), ca);

        attach = ca.getAttach();
        attach.checkChannelOrThread();
        // if (!attach.getChannelHandlerContext().getChannel().isOpen()) {//��Ϊ�������ⲿwirteResponse���������Ի�����Ĭ�ϵ� check
        // log.info("channelClosed     {}:{}", new Object[] { "asyncProxy", attach });
        // return;
        // }
        attach.unregisterCloseable(c);// ȡ��ע�ᣬ���channel�Ѿ�����һ�� �������󣬽�������Ĺ�����

        AsyncCallback callback = ca.getCallback();
        Thread t = Thread.currentThread();
        attach.registerCloseable(t);

        try {
            callback.messageReceived(ctx, e, attach);
        } catch (ProcessFinishedError e1) { // �������̽�����־���������쳣����
        } catch (Throwable e2) {// ���ﲶ����쳣���� ���������˻ذ���ҵ������������������ùرմ�channel
            exceptionCaught(ctx, e2, ca);
        } finally {
            attach.unregisterCloseable(t);
            messageReceivedFinally(ctx, e);
        }
    }

    /**
     * <pre>
     * ���յ���̨��������Ϣ��ҵ������ɵĺ�׺����
     * ����� �����ӣ����ǹر�����
     * ����� ʹ�����ӳ����ĳ����ӣ����ǻ�������
     */
    protected void messageReceivedFinally(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    }

    public abstract AsyncCallbackAttach pollAsyncCallbackAttach(ChannelHandlerContext ctx, MessageEvent e);

    /**
     * ���ͳ����Ϣ
     */
    public StringBuilder printStatInfo(Map<Long, AsyncCallbackAttach> seqMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("processingAttach:[").append(coreMap.size()).append("]").append(coreMapPurgeCount > 0 ? coreMapLastPurgeInfo : "").append("\n");
        long now = System.currentTimeMillis();
        Set<AsyncCallbackAttach> values = new HashSet<AsyncCallbackAttach>();
        List<T> nothingCallbackList = new ArrayList<T>(0);
        for (java.util.Map.Entry<T, AsyncCallbackAttach> e : coreMap.entrySet()) {
            T key = e.getKey();
            AsyncCallbackAttach value = e.getValue();
            if (value == NOTHING_CALLBACK_ATTACH) {
                nothingCallbackList.add(key);
            } else {
                values.add(value);
            }
        }
        if (values.isEmpty()) {
            sb.append("\n");
        }
        for (AsyncCallbackAttach v : values) {
            sb.append(v.getAttach());
            if (v._messageList == null) {
                sb.append("\n");
            } else {
                sb.append("\t").append(HumanReadableUtil.timeSpan(now - v._messageSendTime)).append("\n");
                for (int i = 0; i < v._messageList.size(); i++) {
                    SequenceMessage msg = v._messageList.get(i);
                    SocketAddress addr = v._addressList.get(i);
                    String arrow = seqMap.containsKey(msg.getSequence()) ? "*> " : "=> ";
                    sb.append(arrow).append(addr).append("\t").append(msg).append("\n");
                }
                sb.append("\n");
            }
        }

        if (!nothingCallbackList.isEmpty()) {
            sb.append("----------------------------------------------------\n");
            sb.append("nothingCallbackAttach:[").append(nothingCallbackList.size()).append("]\n");
            for (T key : nothingCallbackList) {
                sb.append(key).append("\n");
            }
        }
        sb.append("----------------------------------------------------\n");
        sb.append(super.printStatInfo());
        return sb;
    }

    /**
     * ���ͳ����Ϣ
     */
    @Override
    public StringBuilder printStatInfo() {
        Map<Long, AsyncCallbackAttach> seqMap = Collections.emptyMap();
        return printStatInfo(seqMap);
    }

    /**
     * �ύ2���첽����
     */
    public void submit(SocketAddress address1, SequenceMessage msg1, SocketAddress address2, SequenceMessage msg2, AsyncCallbackAttach ca) {
        XLContextAttachment attach = ca.getAttach();
        int num = 2;
        if (msg1 == null) {
            num--;
        }
        if (msg2 == null) {
            num--;
        }
        attach.initAsyncMessageEventQueue(num);
        submit(address1, msg1, ca);
        submit(address2, msg2, ca);
    }

    /**
     * �ύ3���첽����
     */
    public void submit(SocketAddress address1, SequenceMessage msg1, SocketAddress address2, SequenceMessage msg2, SocketAddress address3, SequenceMessage msg3, AsyncCallbackAttach ca) {
        XLContextAttachment attach = ca.getAttach();
        int num = 3;
        if (msg1 == null) {
            num--;
        }
        if (msg2 == null) {
            num--;
        }
        if (msg3 == null) {
            num--;
        }
        attach.initAsyncMessageEventQueue(num);
        submit(address1, msg1, ca);
        submit(address2, msg2, ca);
        submit(address3, msg3, ca);
    }

    /**
     * �ύ4���첽����
     */
    public void submit(SocketAddress address1, SequenceMessage msg1, SocketAddress address2, SequenceMessage msg2, SocketAddress address3, SequenceMessage msg3, SocketAddress address4,
            SequenceMessage msg4, AsyncCallbackAttach ca) {
        XLContextAttachment attach = ca.getAttach();
        int num = 4;
        if (msg1 == null) {
            num--;
        }
        if (msg2 == null) {
            num--;
        }
        if (msg3 == null) {
            num--;
        }
        if (msg4 == null) {
            num--;
        }
        attach.initAsyncMessageEventQueue(num);
        submit(address1, msg1, ca);
        submit(address2, msg2, ca);
        submit(address3, msg3, ca);
        submit(address4, msg4, ca);
    }

    /**
     * �ύ5���첽����
     */
    public void submit(SocketAddress address1, SequenceMessage msg1, SocketAddress address2, SequenceMessage msg2, SocketAddress address3, SequenceMessage msg3, SocketAddress address4,
            SequenceMessage msg4, SocketAddress address5, SequenceMessage msg5, AsyncCallbackAttach ca) {
        XLContextAttachment attach = ca.getAttach();
        int num = 5;
        if (msg1 == null) {
            num--;
        }
        if (msg2 == null) {
            num--;
        }
        if (msg3 == null) {
            num--;
        }
        if (msg4 == null) {
            num--;
        }
        if (msg5 == null) {
            num--;
        }
        attach.initAsyncMessageEventQueue(num);
        submit(address1, msg1, ca);
        submit(address2, msg2, ca);
        submit(address3, msg3, ca);
        submit(address4, msg4, ca);
        submit(address5, msg5, ca);
    }

    /**
     * �ύ6���첽����
     */
    public void submit(SocketAddress address1, SequenceMessage msg1, SocketAddress address2, SequenceMessage msg2, SocketAddress address3, SequenceMessage msg3, SocketAddress address4,
            SequenceMessage msg4, SocketAddress address5, SequenceMessage msg5, SocketAddress address6, SequenceMessage msg6, AsyncCallbackAttach ca) {
        XLContextAttachment attach = ca.getAttach();
        int num = 6;
        if (msg1 == null) {
            num--;
        }
        if (msg2 == null) {
            num--;
        }
        if (msg3 == null) {
            num--;
        }
        if (msg4 == null) {
            num--;
        }
        if (msg5 == null) {
            num--;
        }
        if (msg6 == null) {
            num--;
        }
        attach.initAsyncMessageEventQueue(num);
        submit(address1, msg1, ca);
        submit(address2, msg2, ca);
        submit(address3, msg3, ca);
        submit(address4, msg4, ca);
        submit(address5, msg5, ca);
        submit(address6, msg6, ca);
    }

    /**
     * �ύ7���첽����
     */
    public void submit(SocketAddress address1, SequenceMessage msg1, SocketAddress address2, SequenceMessage msg2, SocketAddress address3, SequenceMessage msg3, SocketAddress address4,
            SequenceMessage msg4, SocketAddress address5, SequenceMessage msg5, SocketAddress address6, SequenceMessage msg6, SocketAddress address7, SequenceMessage msg7, AsyncCallbackAttach ca) {
        XLContextAttachment attach = ca.getAttach();
        int num = 7;
        if (msg1 == null) {
            num--;
        }
        if (msg2 == null) {
            num--;
        }
        if (msg3 == null) {
            num--;
        }
        if (msg4 == null) {
            num--;
        }
        if (msg5 == null) {
            num--;
        }
        if (msg6 == null) {
            num--;
        }
        if (msg7 == null) {
            num--;
        }
        attach.initAsyncMessageEventQueue(num);
        submit(address1, msg1, ca);
        submit(address2, msg2, ca);
        submit(address3, msg3, ca);
        submit(address4, msg4, ca);
        submit(address5, msg5, ca);
        submit(address6, msg6, ca);
        submit(address7, msg7, ca);
    }

    /**
     * �ύ8���첽����
     */
    public void submit(SocketAddress address1, SequenceMessage msg1, SocketAddress address2, SequenceMessage msg2, SocketAddress address3, SequenceMessage msg3, SocketAddress address4,
            SequenceMessage msg4, SocketAddress address5, SequenceMessage msg5, SocketAddress address6, SequenceMessage msg6, SocketAddress address7, SequenceMessage msg7, SocketAddress address8,
            SequenceMessage msg8, AsyncCallbackAttach ca) {
        XLContextAttachment attach = ca.getAttach();
        int num = 8;
        if (msg1 == null) {
            num--;
        }
        if (msg2 == null) {
            num--;
        }
        if (msg3 == null) {
            num--;
        }
        if (msg4 == null) {
            num--;
        }
        if (msg5 == null) {
            num--;
        }
        if (msg6 == null) {
            num--;
        }
        if (msg7 == null) {
            num--;
        }
        if (msg8 == null) {
            num--;
        }
        attach.initAsyncMessageEventQueue(num);
        submit(address1, msg1, ca);
        submit(address2, msg2, ca);
        submit(address3, msg3, ca);
        submit(address4, msg4, ca);
        submit(address5, msg5, ca);
        submit(address6, msg6, ca);
        submit(address7, msg7, ca);
        submit(address8, msg8, ca);
    }

    /**
     * �ύ9���첽����
     */
    public void submit(SocketAddress address1, SequenceMessage msg1, SocketAddress address2, SequenceMessage msg2, SocketAddress address3, SequenceMessage msg3, SocketAddress address4,
            SequenceMessage msg4, SocketAddress address5, SequenceMessage msg5, SocketAddress address6, SequenceMessage msg6, SocketAddress address7, SequenceMessage msg7, SocketAddress address8,
            SequenceMessage msg8, SocketAddress address9, SequenceMessage msg9, AsyncCallbackAttach ca) {
        XLContextAttachment attach = ca.getAttach();
        int num = 9;
        if (msg1 == null) {
            num--;
        }
        if (msg2 == null) {
            num--;
        }
        if (msg3 == null) {
            num--;
        }
        if (msg4 == null) {
            num--;
        }
        if (msg5 == null) {
            num--;
        }
        if (msg6 == null) {
            num--;
        }
        if (msg7 == null) {
            num--;
        }
        if (msg8 == null) {
            num--;
        }
        if (msg9 == null) {
            num--;
        }
        attach.initAsyncMessageEventQueue(num);
        submit(address1, msg1, ca);
        submit(address2, msg2, ca);
        submit(address3, msg3, ca);
        submit(address4, msg4, ca);
        submit(address5, msg5, ca);
        submit(address6, msg6, ca);
        submit(address7, msg7, ca);
        submit(address8, msg8, ca);
        submit(address9, msg9, ca);
    }

    /**
     * �ύ10���첽����
     */
    public void submit(SocketAddress address1, SequenceMessage msg1, SocketAddress address2, SequenceMessage msg2, SocketAddress address3, SequenceMessage msg3, SocketAddress address4,
            SequenceMessage msg4, SocketAddress address5, SequenceMessage msg5, SocketAddress address6, SequenceMessage msg6, SocketAddress address7, SequenceMessage msg7, SocketAddress address8,
            SequenceMessage msg8, SocketAddress address9, SequenceMessage msg9, SocketAddress address10, SequenceMessage msg10, AsyncCallbackAttach ca) {
        XLContextAttachment attach = ca.getAttach();
        int num = 10;
        if (msg1 == null) {
            num--;
        }
        if (msg2 == null) {
            num--;
        }
        if (msg3 == null) {
            num--;
        }
        if (msg4 == null) {
            num--;
        }
        if (msg5 == null) {
            num--;
        }
        if (msg6 == null) {
            num--;
        }
        if (msg7 == null) {
            num--;
        }
        if (msg8 == null) {
            num--;
        }
        if (msg9 == null) {
            num--;
        }
        if (msg10 == null) {
            num--;
        }
        attach.initAsyncMessageEventQueue(num);
        submit(address1, msg1, ca);
        submit(address2, msg2, ca);
        submit(address3, msg3, ca);
        submit(address4, msg4, ca);
        submit(address5, msg5, ca);
        submit(address6, msg6, ca);
        submit(address7, msg7, ca);
        submit(address8, msg8, ca);
        submit(address9, msg9, ca);
        submit(address9, msg10, ca);
    }

    /**
     * �����submit(SocketAddress backstageHostAddress, SequenceMessage msg, AsyncCallbackAttach ca)
     */
    @Deprecated
    public void submit(SocketAddress backstageHostAddress, SequenceMessage msg, XLContextAttachment attach, AsyncCallbackAttach asyncCallbackAttach) {
        submit(backstageHostAddress, msg, asyncCallbackAttach);
    }

    /**
     * ����ע�⣬����Ҫ����AsyncCallback�ǵ��������Ҳ��� AsyncCallbackAttach�ύ�첽����
     */
    public void submit(SocketAddress backstageHostAddress, SequenceMessage msg, XLContextAttachment attach, AsyncCallback asyncCallback) {
        attach.checkChannelOrThread();
        if (asyncCallback instanceof AsyncCallbackAttach) {
            submit(backstageHostAddress, msg, (AsyncCallbackAttach) asyncCallback);
        } else {
            submit(backstageHostAddress, msg, new AsyncCallbackAttach(attach, asyncCallback));
        }
    }

    /**
     * ����һ������Ҫ����ذ�������
     */
    public void submit(SocketAddress backstageHostAddress, SequenceMessage msg) {
        submit(backstageHostAddress, msg, NOTHING_CALLBACK_ATTACH);
    }

    public abstract void submit(SocketAddress backstageHostAddress, SequenceMessage msg, AsyncCallbackAttach ca);
}
