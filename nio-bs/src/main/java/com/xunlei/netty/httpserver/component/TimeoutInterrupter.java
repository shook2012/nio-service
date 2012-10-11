package com.xunlei.netty.httpserver.component;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xunlei.netty.httpserver.handler.TextResponseHandlerManager;
import com.xunlei.spring.Config;
import com.xunlei.util.Log;
import com.xunlei.util.concurrent.ConcurrentHashSet;
import com.xunlei.util.concurrent.ConcurrentUtil;

/**
 * <pre>
 * ע�⣺
 * 1.���һ��ʼdisableʱ,����������ʱ�ٴ�ʱ,��Ϊԭ�����ֵĳ�����û��ע�ᵽliveAttach��,��Щ�����Ӳ��ᱻTimeoutInterrupterɨ�赽���ر�
 * 2.keepAliveTimeout���������ж��Ƿ��TimeoutInterrupter������
 * 
 * @author ZengDong
 * @since 2011-3-17 ����09:56:59
 */
@Service
public class TimeoutInterrupter {

    @Autowired
    protected TextResponseHandlerManager handlerManager;

    public interface AttachRegister {

        public void registerAttach(XLContextAttachment attach);

        public void unregisterAttach(XLContextAttachment attach);
    }

    private static final Logger log = Log.getLogger();
    private Runnable _defaultInterrupter = new Runnable() {

        private void close(XLContextAttachment attach, String tips) {
            Channel channel = attach.getChannelHandlerContext().getChannel();
            channel.close();
            sweepedChannelNum++;
            log.warn("close {} attach:{}", tips, attach);
        }

        /**
         * <pre>
         *       |<-----------------------------------ReaderIdleTimeout-------------------------------->|
         *                                                                      |<----------------------------------------WriterIdleTimeout---------------------------->|
         * messageReceived - processThreadBegin ------- processThreadEnd - writeComplete ---------messageReceived - processThreadBegin ------- processThreadEnd - writeComplete
         *                           |<-------cmdTimeout------>|                |<---AllIdleTimeout---->|
         *                                                                      |<---keepAliveTimeout-->|
         * 
         * ����ͼ���Կ���,һ������£�readerIdleTimeSeconds,writerIdleTimeSeconds > allIdleTimeSeconds > keepAliveTimeout
         */
        public void run() {
            // log.info("start TimeoutInterrupter,liveAttachNum:{}", currentChannelsNum());
            for (XLContextAttachment attach : liveAttach) {
                if (attach.isNotProcessing()) {// �ж����Ƿ�io����
                    long read = attach.getLastReadTime();
                    long write = attach.getLastWriteTime();
                    long all = Math.max(read, write);
                    int allTimeout = allIdleTimeSeconds;
                    long now = System.currentTimeMillis();
                    XLHttpResponse response = attach.getResponse();
                    if (response == null) {// ���ڰ汾���Ƶú��ϸ�,����ֻҪ���� response�ǿ�,readerIdleTimeSeconds��writerIdleTimeSeconds������������
                        if (readerIdleTimeSeconds > 0) {
                            if (now - read > readerIdleTimeSeconds * 1000) {
                                close(attach, "readIdle-" + readerIdleTimeSeconds);
                                continue;
                            }
                        }
                        if (writerIdleTimeSeconds > 0) {
                            if (now - write > writerIdleTimeSeconds * 1000) {
                                close(attach, "writeIdle-" + writerIdleTimeSeconds);
                                continue;
                            }
                        }
                    } else {
                        int keepAliveTimeout = response.getKeepAliveTimeout();
                        allTimeout = Math.max(allIdleTimeSeconds, keepAliveTimeout); // ���������ȡ������������ڵ�attach
                    }

                    if (allTimeout > 0) {// ���������ҪallIdle timeout
                        if (now - all > allTimeout * 1000) {
                            close(attach, "allIdle-" + allTimeout);
                            continue;
                        }
                    }
                } else {// ˵������ҵ����
                    long timeout = attach.getCmdMeta().getTimeout();
                    if (!attach.getChannelHandlerContext().getChannel().isOpen()) {// Զ���Ѿ��ر��˴�channel,�����ж�������߳�
                        StringBuilder info = new StringBuilder();
                        attach.interrupt(info);
                        log.warn("interrupt {} [channelClosed] {}", new Object[] { attach, info });// TODO:����ͳ�Ʋ���config��"ͨ������ǰ�ر�:"
                    } else if (timeout > 0) {
                        long span = System.currentTimeMillis() - attach.getProcess();
                        if (span > timeout * 1000) {
                            StringBuilder info = new StringBuilder();
                            attach.interrupt(info);// ֪ͨ�ر�����closable���߳� //TODO:�����Ƿ񷵻����ݵ��ͻ���
                            log.warn("interrupt {} [{}ms] {}", new Object[] { attach, span, info });
                        }
                    }
                }
            }
        }
    };
    public final AttachRegister _defaultRegister = new AttachRegister() {

        public void registerAttach(XLContextAttachment attach) {
            liveAttach.add(attach);
        }

        public void unregisterAttach(XLContextAttachment attach) {
            if (!liveAttach.remove(attach)) {// û��remove�ɹ��������־�������
                log.error("unregisterAttach fail,attach:\n{}", attach.getDetailInfo());
            }
        }
    };

    public final AttachRegister _nopRegister = new AttachRegister() {

        public void registerAttach(XLContextAttachment attach) {
        }

        public void unregisterAttach(XLContextAttachment attach) {
        }
    };
    @Config(resetable = true)
    public volatile int allIdleTimeSeconds = 0;
    private AttachRegister attachRegister = _nopRegister;
    /**
     * ���л������������ڵ�attach
     */
    private final Set<XLContextAttachment> liveAttach = new ConcurrentHashSet<XLContextAttachment>();
    @Config(resetable = true)
    public volatile int readerIdleTimeSeconds = 0;
    private ScheduledFuture<?> scheduledFuture;
    private volatile long sweepedChannelNum;
    private Boolean threadInterrupterEnable;
    @Config(resetable = true)
    public volatile int writerIdleTimeSeconds = 0;

    public int currentChannelsNum() {
        return liveAttach.size();
    }

    public AttachRegister getAttachRegister() {
        return attachRegister;
    }

    public boolean isEnable() {
        return threadInterrupterEnable || readerIdleTimeSeconds > 0 || allIdleTimeSeconds > 0 || writerIdleTimeSeconds > 0;
    }

    private synchronized boolean reset() {
        // ��nettyHttpServer����ʱ������ spring�������ڼ����һ�Σ�����
        // setThreadInterrupterEnable<-CmdMapper.resetCmdConfig<-CmdMapperDispatcher.init() ����һ��
        // Ϊ������־ֻ��ӡһ�Σ���threadInterrupterEnable
        // ��û�г�ʼ��������£�����
        if (threadInterrupterEnable == null)
            return false;
        boolean result = isEnable();
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }

        if (result) {
            attachRegister = _defaultRegister;
            log.warn("TimeoutInterrupter      ON,ioIdle:{},{},{},cmdTimeout:{}", new Object[] { readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, threadInterrupterEnable });
            scheduledFuture = ConcurrentUtil.getDaemonExecutor().scheduleWithFixedDelay(_defaultInterrupter, 100, 100, TimeUnit.MILLISECONDS);
        } else {
            attachRegister = _nopRegister;
            liveAttach.clear();
            // sweepedChannelNum ����
            log.warn("TimeoutInterrupter      OFF");
        }
        return result;
    }

    public void setThreadInterrupterEnable(boolean threadInterrupterEnable) {
        int ori = hashCode();
        this.threadInterrupterEnable = threadInterrupterEnable;
        int now = hashCode();
        if (ori != now) {
            reset();
        }
    }

    public long sweepedChannelNum() {
        return sweepedChannelNum;
    }

    public synchronized void sweepedChannelNumIncr(int count) {
        sweepedChannelNum += count;
    }

    public void setAllIdleTimeSeconds(int allIdleTimeSeconds) {// ʵ�ֿ�ʵʱ����,���﷢�� ��ֵ�б䶯
        int ori = hashCode();
        this.allIdleTimeSeconds = allIdleTimeSeconds;
        int now = hashCode();
        if (ori != now) {
            reset();
        }
    }

    public void setReaderIdleTimeSeconds(int readerIdleTimeSeconds) { // ʵ�ֿ�ʵʱ����,���﷢�� ��ֵ�б䶯
        int ori = hashCode();
        this.readerIdleTimeSeconds = readerIdleTimeSeconds;
        int now = hashCode();
        if (ori != now) {
            reset();
        }
    }

    public void setWriterIdleTimeSeconds(int writerIdleTimeSeconds) { // ʵ�ֿ�ʵʱ����,���﷢�� ��ֵ�б䶯
        int ori = hashCode();
        this.writerIdleTimeSeconds = writerIdleTimeSeconds;
        int now = hashCode();
        if (ori != now) {
            reset();
        }
    }

    public Set<XLContextAttachment> getLiveAttach() {
        return liveAttach;
    }

    public void removeAttach(XLContextAttachment attach) {
        liveAttach.remove(attach);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + allIdleTimeSeconds;
        result = prime * result + readerIdleTimeSeconds;
        result = prime * result + ((threadInterrupterEnable == null) ? 0 : threadInterrupterEnable.hashCode());
        result = prime * result + writerIdleTimeSeconds;
        return result;
    }

}
