package com.xunlei.netty.httpserver.cmd.common;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.channel.socket.nio.NioWorkerStat;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xunlei.netty.httpserver.cmd.BaseStatCmd;
import com.xunlei.netty.httpserver.cmd.CmdMappers;
import com.xunlei.netty.httpserver.cmd.annotation.CmdCategory;
import com.xunlei.netty.httpserver.component.TimeoutInterrupter;
import com.xunlei.netty.httpserver.component.XLContextAttachment;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import com.xunlei.netty.httpserver.component.XLHttpResponse.ContentType;
import com.xunlei.netty.httpserver.util.HttpServerConfig;
import com.xunlei.netty.httpserver.util.Statistics;
import com.xunlei.netty.httpserver.util.StatisticsHelper;
import com.xunlei.netty.httpserver.util.StatisticsHelper.Snapshot;
import com.xunlei.netty.httpserver.util.TimeSpanStatHelper;
import com.xunlei.spring.AfterConfig;
import com.xunlei.util.HumanReadableUtil;
import com.xunlei.util.Log;
import com.xunlei.util.ManifestInfo;
import com.xunlei.util.StringHelper;
import com.xunlei.util.StringTools;
import com.xunlei.util.SystemInfo;
import com.xunlei.util.concurrent.ConcurrentUtil;
import com.xunlei.util.stat.TimeSpanStat;

/**
 * <pre>
 *  �������: 55
 * 	 ��Ӧ����: 53
 * 	 ͨ����ǰ�ر�: 5
 * 	 ������� - ��Ӧ���� = ͨ����ǰ�ر� - ClosedChannelError(��ǰ��鵽ͨ���رյĴ���)
 * 
 *  ͨ���򿪴���: 77
 * 	 ͨ���رմ���: 76
 * 	 ��ǰ��ͨ��: 1
 * 	 ���������ͨ��: 0
 * 	 ��ǰ��ͨ�� = ͨ���򿪴��� - ͨ���رմ���
 * 	 �ͻ��˻�������� �����رմ��� = ͨ���رմ��� - ���������ͨ��
 * 	
 * 	 401IP����: 0			ip���˲�ͨ���Ĵ���,ipfilter
 * 	 403��ֹ����: 26			favicon.ico����Ĵ���
 * 	 404δ�ҵ�: 0			�Ҳ���cmd��򷽷��Ĵ���
 * 	 408����ʱ: 0			�ڲ�future.get(toleranceTimeout)��ʱ�Ĵ���
 * 	 500�ڲ�����: 0			ҵ����������
 * </pre>
 * 
 * <pre>
 * ��ʼ����		attach.decode			(req.createTime)		
 * 									} ������ʱ
 * ��ʼҵ����	attach.process			(resp.createTime)
 * 									} ҵ������ʱ
 * ��ʼ����		attach.encode
 * 									} ������ʱ
 * �������		attach.complete
 * </pre>
 * 
 * @author ZengDong
 * @since 2010-5-20 ����03:34:28
 */
@Service
@CmdCategory("system")
public class StatCmd extends BaseStatCmd implements Statistics {

    /**
     * ʱ��ͳ��
     * 
     * @author ZengDong
     * @since 2010-5-23 ����09:21:36
     */
    public class StageTimeSpanStat extends TimeSpanStat {

        public StageTimeSpanStat(String name, Logger log) {
            super(name, log);
            this.initFormat(21, 2);
        }

        public StageTimeSpanStat(String name) {
            super(name, 1000, false, null);
            this.initFormat(21, 2);
        }

        private void recordForWriteComplete(long incr, long end, long begin, XLContextAttachment attach) {
            if (incr <= 0 || end <= 0 || begin <= 0)// ��Ӧ�ú���
                return;

            long span = end - begin;
            long lastSpan = span - incr;
            all_span.addAndGet(incr);
            if (span >= slowThreshold) {
                if (lastSpan >= slowThreshold) {
                    // ˵���ϴα���ʱ���Ѿ�����
                    slow_span.addAndGet(incr);
                    // log.error("SLOW_PROCESS_INCR:{} Using {}ms --> uri:{}", new Object[] { name, span, attach.getRequest().getUri() });
                } else {
                    slow_span.addAndGet(span);
                    slow_num.incrementAndGet();
                    // log.error("SLOW_PROCESS:{} Using {}ms --> uri:{}", new Object[] { name, span, attach.getRequest().getUri() });
                }
            }
            if (span > max_span)
                max_span = span;
        }

        @Override
        protected void warn(long end, long begin, Object arg) {
            XLContextAttachment attach = (XLContextAttachment) arg;
            log.error("SLOW_PROCESS:{}:{} [{}ms][{}]\n[uri:{}]", new Object[] { name, arg, end - begin, HumanReadableUtil.byteSize(attach.getResponse().getContentLength()),
                    attach.getRequest().getUri() });
        }
    }

    /**
     * ����ͳ��
     * 
     * @author ZengDong
     * @since 2010-5-23 ����09:21:23
     */
    public class StreamStat {

        private AtomicLong bytes = new AtomicLong();
        private volatile long max;
        private String name;
        private AtomicLong num = new AtomicLong();

        public StreamStat(String name) {
            this.name = name;
        }

        private void record(long byte_len) {
            num.incrementAndGet();
            bytes.addAndGet(byte_len);
            if (byte_len > max)
                max = byte_len;
        }

        @Override
        public String toString() {
            long numTmp = num.get();
            long bytesTmp = bytes.get();
            long avg = numTmp > 0 ? bytesTmp / numTmp : 0;
            return String.format(streamStatFmt, name, numTmp, HumanReadableUtil.byteSize(bytesTmp), HumanReadableUtil.byteSize(avg), HumanReadableUtil.byteSize(max));
        }
    }

    private static final Logger log = Log.getLogger();
    private static final String streamStatFmt = "%-21s %-8s %-20s %-19s %-20s\n";
    private StageTimeSpanStat allTSS;
    private AtomicLong channelCloses;// ���йرյ�ͨ����
    private AtomicLong channelInterruptCloses;// ͨ������ǰ�رյĴ���
    private AtomicLong channelOpens;
    @Autowired
    private TimeoutInterrupter timeoutInterrupter;
    @Autowired
    private HttpServerConfig config;
    private StageTimeSpanStat decodeTSS;
    private StageTimeSpanStat encodeTSS;
    private StreamStat inbound;
    private StageTimeSpanStat okTSS;
    private StreamStat outbound;
    private StageTimeSpanStat processTSS;
    private AtomicLong reqs_401;
    private AtomicLong reqs_403;
    private AtomicLong reqs_404;
    private AtomicLong reqs_500;
    private AtomicLong reqs_503;
    private long statBeginTime;

    @Autowired
    private CmdMappers cmdMappers;

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
        channelCloses.incrementAndGet();
    }

    @Override
    public void channelInterruptClosed(ChannelHandlerContext ctx) {
        channelInterruptCloses.incrementAndGet();
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
        channelOpens.incrementAndGet();
    }

    public String getExecutorInfo() {
        return ConcurrentUtil.getAllExecutorInfo(config.getPipelineExecutorUnordered(), config.getPipelineExecutorOrdered());
    }

    public String getRuntimeInfo() {
        StringBuilder tmp = new StringBuilder();

        tmp.append("STATͳ��ʱ��:\t\t").append(HumanReadableUtil.timeSpan(System.currentTimeMillis() - getStatBeginTime())).append("\n");
        tmp.append("ͨ���򿪴���:\t\t").append(channelOpens.get()).append("\n");
        tmp.append("ͨ���رմ���:\t\t").append(channelCloses.get()).append("\n");
        tmp.append("��ǰ��ͨ��:\t\t").append(timeoutInterrupter.currentChannelsNum()).append("\n");
        tmp.append("���������ͨ��:\t\t").append(timeoutInterrupter.sweepedChannelNum()).append("\n");
        tmp.append("ͨ������ǰ�ر�:\t\t").append(channelInterruptCloses.get()).append("\n");

        tmp.append("\n");
        tmp.append(String.format(streamStatFmt, "���", "times", "all_bytes", "avg_bytes", "max"));
        tmp.append(inbound);
        tmp.append(outbound);

        tmp.append("\n");
        tmp.append(decodeTSS.getTableHeader());
        tmp.append(decodeTSS);
        tmp.append(processTSS);
        tmp.append(encodeTSS);
        tmp.append(allTSS);
        tmp.append(okTSS);
        tmp.append("401IP����:\t\t").append(reqs_401).append("\n");
        tmp.append("403��ֹ����:\t\t").append(reqs_403).append("\n");
        tmp.append("404δ�ҵ�:\t\t").append(reqs_404).append("\n");
        tmp.append("500�ڲ�����:\t\t").append(reqs_500).append("\n");
        tmp.append("503����ʱ:\t\t").append(reqs_503).append("\n");

        return tmp.toString();
    }

    public long getStatBeginTime() {
        return statBeginTime;
    }

    @Override
    public void messageReceived(XLContextAttachment attach) {
        decodeTSS.record(attach.getProcess(), attach.getDecode(), attach);
    }

    @Override
    public void messageReceiving(MessageEvent e) {
        if (e.getMessage() instanceof ChannelBuffer) {
            ChannelBuffer b = (ChannelBuffer) e.getMessage();
            inbound.record(b.capacity());
        }
    }

    public Object nioworkers(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        return NioWorkerStat.statNioWorkers();
    }

    public Object nioworkersAll(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        return NioWorkerStat.statNioWorkersAll();
    }

    public Object process(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);

        StringBuilder tmp = new StringBuilder();
        tmp.append(getRuntimeInfo());

        tmp.append(StringHelper.printLine(160, '-'));
        tmp.append(getExecutorInfo());
        tmp.append(StringHelper.printLine(160, '-'));
        tmp.append(SystemInfo.getSytemInfo());
        tmp.append(StringHelper.printLine(160, '-'));
        appendManifestInfo(tmp);
        return tmp.toString();
    }

    public void appendManifestInfo(StringBuilder tmp) {
        tmp.append("RUIZ POWERED BY\t\t");
        List<ManifestInfo> core = ManifestInfo.getManifestInfoCore();
        List<ManifestInfo> other = ManifestInfo.getManifestInfoOther();
        tmp.append(Integer.toHexString(core.hashCode())).append("-").append(Integer.toHexString(other.hashCode())).append("\n");
        for (ManifestInfo m : core) {
            tmp.append(m).append("\n");
        }
    }

    @Autowired
    private StatisticsHelper statisticsHelper;
    @Autowired
    private TimeSpanStatHelper timeSpanStatHelper;

    public Object timespan(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        response.setInnerContentType(ContentType.html);
        return timeSpanStatHelper.getInfo();
    }

    public Object tps(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        int sampleSize = request.getParameterInteger("size", 20);// TODO:Ĭ�ϴ�ӡ���20��tps��¼
        int interval = request.getParameterInteger("interval", 600);// TODO:Ĭ��10minͳ��һ��
        boolean calcTps = request.getParameterBoolean("tps", true);
        List<Snapshot> r = statisticsHelper.getSnapshot(sampleSize, interval * 1000, calcTps);
        StringBuilder tmp = new StringBuilder();
        for (Snapshot dss : r) {
            tmp.append(dss).append("\n");
        }
        tmp.append("\n");

        if (calcTps) {
            tmp.append("------HISTORY MAX------\n");
            tmp.append(statisticsHelper.getMaxTps());
        }

        // tmp.append("\n");
        // tmp.append(statisticsHelper);
        return tmp.toString();
    }

    /**
     * <pre>
     * ��������ͳ����
     * 
     * ע��,����û�н��в�������
     * Ҳ���������ù�����,HttpServer�����������������
     * Ӱ�첻��,���Բ��Դ�������д���
     */
    @AfterConfig
    public void reset() {
        statBeginTime = System.currentTimeMillis();
        channelOpens = new AtomicLong();
        channelCloses = new AtomicLong();// ���йرյ�ͨ����
        channelInterruptCloses = new AtomicLong();// ����ǰ�رյ�ͨ����

        reqs_401 = new AtomicLong();
        reqs_503 = new AtomicLong();
        reqs_403 = new AtomicLong();
        reqs_404 = new AtomicLong();
        reqs_500 = new AtomicLong();

        decodeTSS = new StageTimeSpanStat("����");
        processTSS = new StageTimeSpanStat("ҵ��");
        encodeTSS = new StageTimeSpanStat("����");

        allTSS = new StageTimeSpanStat("����", log);
        okTSS = new StageTimeSpanStat("200�ɹ�");

        outbound = new StreamStat("����");
        inbound = new StreamStat("���");
        cmdMappers.resetAllCounter();// �����������������
    }

    public Object threads(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        int max_frames = request.getParameterInteger("maxFrames", 16);
        boolean onlyRunnable = request.getParameterBoolean("runnable", false);
        String name = request.getParameter("name", "");
        return SystemInfo.getThreadsDetailInfo(name, onlyRunnable, max_frames);
    }

    public Object manifest(XLHttpRequest request, XLHttpResponse response) throws Exception {
        // init(request, response);
        response.setInnerContentType(ContentType.plain);
        boolean include = true;
        String jarFileStartWith = request.getParameter("startWith", "");
        if (StringTools.isEmpty(jarFileStartWith)) {
            jarFileStartWith = request.getParameter("notStartWith", "");
            if (StringTools.isNotEmpty(jarFileStartWith))
                include = false;
        }
        List<ManifestInfo> l = ManifestInfo.getManifestInfo(jarFileStartWith, include);
        StringBuilder sb = new StringBuilder();
        sb.append("HASHCODE\t\t").append(Integer.toHexString(l.hashCode())).append("\n");

        for (ManifestInfo m : l) {
            sb.append(m).append("\n");
        }
        return sb;
    }

    @Override
    public void writeBegin(XLContextAttachment attach) {
        attach.markWriteBegin();
        processTSS.record(attach.getEncode(), attach.getProcess(), attach);
        // attach.getCmdMeta().access();//�����������,��Ҫ����cmdMeta�����ڵ����
        // ���Ի��Ƿŵ� CmdMapperDispatcher���õ�cmdMetaʱ������,������ɵĺ��ֻ������ �ڴ��������Ҳ���cmdMetaʱ,setting/cmds����İٷֱ��Ǵ��
    }

    @Override
    public void writeComplete(XLContextAttachment attach, WriteCompletionEvent e) {
        outbound.record(e.getWrittenAmount());
        XLHttpResponse resp = attach.getResponse();
        if (resp == null) {// https �����һ��ʼ������ resp
            return;
        }
        // TODO:����û�д���chunk�����
        long incr = attach.markWriteEnd();
        long complete = attach.getComplete();

        if (incr == -1) {
            // ˵���ǵ�һ��д
            encodeTSS.record(complete, attach.getEncode(), attach);
            allTSS.record(complete, attach.getDecode(), attach);
            switch (resp.getStatus().getCode()) {
            case 200:
                okTSS.record(complete, attach.getDecode(), attach);
                break;
            case 503:
                reqs_503.incrementAndGet();
                break;
            case 401:
                reqs_401.incrementAndGet();
                break;
            case 403:
                reqs_403.incrementAndGet();
                break;
            case 404:
                reqs_404.incrementAndGet();
                break;
            case 500:
                reqs_500.incrementAndGet();
                break;
            }
        } else {
            encodeTSS.recordForWriteComplete(incr, complete, attach.getEncode(), attach);
            allTSS.recordForWriteComplete(incr, complete, attach.getDecode(), attach);
            if (resp.getStatus().getCode() == 200)
                okTSS.recordForWriteComplete(incr, complete, attach.getDecode(), attach);
        }
    }

    public StageTimeSpanStat getProcessTSS() {
        return processTSS;
    }
}
