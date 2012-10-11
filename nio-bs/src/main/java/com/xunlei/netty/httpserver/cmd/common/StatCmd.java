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
 *  请求次数: 55
 * 	 响应次数: 53
 * 	 通道提前关闭: 5
 * 	 请求次数 - 响应次数 = 通道提前关闭 - ClosedChannelError(提前检查到通道关闭的次数)
 * 
 *  通道打开次数: 77
 * 	 通道关闭次数: 76
 * 	 当前打开通道: 1
 * 	 服务器清除通道: 0
 * 	 当前打开通道 = 通道打开次数 - 通道关闭次数
 * 	 客户端或服务器端 正常关闭次数 = 通道关闭次数 - 服务器清除通道
 * 	
 * 	 401IP受限: 0			ip过滤不通过的次数,ipfilter
 * 	 403禁止访问: 26			favicon.ico请求的次数
 * 	 404未找到: 0			找不到cmd类或方法的次数
 * 	 408处理超时: 0			内部future.get(toleranceTimeout)超时的次数
 * 	 500内部错误: 0			业务处理错误次数
 * </pre>
 * 
 * <pre>
 * 开始解码		attach.decode			(req.createTime)		
 * 									} 解码用时
 * 开始业务处理	attach.process			(resp.createTime)
 * 									} 业务处理用时
 * 开始编码		attach.encode
 * 									} 编码用时
 * 发送完成		attach.complete
 * </pre>
 * 
 * @author ZengDong
 * @since 2010-5-20 下午03:34:28
 */
@Service
@CmdCategory("system")
public class StatCmd extends BaseStatCmd implements Statistics {

    /**
     * 时长统计
     * 
     * @author ZengDong
     * @since 2010-5-23 上午09:21:36
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
            if (incr <= 0 || end <= 0 || begin <= 0)// 都应该忽略
                return;

            long span = end - begin;
            long lastSpan = span - incr;
            all_span.addAndGet(incr);
            if (span >= slowThreshold) {
                if (lastSpan >= slowThreshold) {
                    // 说明上次编码时就已经慢了
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
     * 流量统计
     * 
     * @author ZengDong
     * @since 2010-5-23 上午09:21:23
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
    private AtomicLong channelCloses;// 所有关闭的通道数
    private AtomicLong channelInterruptCloses;// 通道被提前关闭的次数
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

        tmp.append("STAT统计时长:\t\t").append(HumanReadableUtil.timeSpan(System.currentTimeMillis() - getStatBeginTime())).append("\n");
        tmp.append("通道打开次数:\t\t").append(channelOpens.get()).append("\n");
        tmp.append("通道关闭次数:\t\t").append(channelCloses.get()).append("\n");
        tmp.append("当前打开通道:\t\t").append(timeoutInterrupter.currentChannelsNum()).append("\n");
        tmp.append("服务器清除通道:\t\t").append(timeoutInterrupter.sweepedChannelNum()).append("\n");
        tmp.append("通道被提前关闭:\t\t").append(channelInterruptCloses.get()).append("\n");

        tmp.append("\n");
        tmp.append(String.format(streamStatFmt, "类别", "times", "all_bytes", "avg_bytes", "max"));
        tmp.append(inbound);
        tmp.append(outbound);

        tmp.append("\n");
        tmp.append(decodeTSS.getTableHeader());
        tmp.append(decodeTSS);
        tmp.append(processTSS);
        tmp.append(encodeTSS);
        tmp.append(allTSS);
        tmp.append(okTSS);
        tmp.append("401IP受限:\t\t").append(reqs_401).append("\n");
        tmp.append("403禁止访问:\t\t").append(reqs_403).append("\n");
        tmp.append("404未找到:\t\t").append(reqs_404).append("\n");
        tmp.append("500内部错误:\t\t").append(reqs_500).append("\n");
        tmp.append("503处理超时:\t\t").append(reqs_503).append("\n");

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
        int sampleSize = request.getParameterInteger("size", 20);// TODO:默认打印最近20个tps记录
        int interval = request.getParameterInteger("interval", 600);// TODO:默认10min统计一次
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
     * 重置所有统计项
     * 
     * 注意,这里没有进行并发处理
     * 也就是在重置过程中,HttpServer还能允许新请求进来
     * 影响不大,所以不对此问题进行处理
     */
    @AfterConfig
    public void reset() {
        statBeginTime = System.currentTimeMillis();
        channelOpens = new AtomicLong();
        channelCloses = new AtomicLong();// 所有关闭的通道数
        channelInterruptCloses = new AtomicLong();// 被提前关闭的通道数

        reqs_401 = new AtomicLong();
        reqs_503 = new AtomicLong();
        reqs_403 = new AtomicLong();
        reqs_404 = new AtomicLong();
        reqs_500 = new AtomicLong();

        decodeTSS = new StageTimeSpanStat("解码");
        processTSS = new StageTimeSpanStat("业务");
        encodeTSS = new StageTimeSpanStat("编码");

        allTSS = new StageTimeSpanStat("所有", log);
        okTSS = new StageTimeSpanStat("200成功");

        outbound = new StreamStat("发包");
        inbound = new StreamStat("解包");
        cmdMappers.resetAllCounter();// 重置所有命令计数器
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
        // attach.getCmdMeta().access();//如果放在这里,还要考虑cmdMeta不存在的情况
        // 所以还是放到 CmdMapperDispatcher刚拿到cmdMeta时来计数,这样造成的后果只不过是 在大量请求找不到cmdMeta时,setting/cmds里面的百分比是错的
    }

    @Override
    public void writeComplete(XLContextAttachment attach, WriteCompletionEvent e) {
        outbound.record(e.getWrittenAmount());
        XLHttpResponse resp = attach.getResponse();
        if (resp == null) {// https 情况，一开始不会有 resp
            return;
        }
        // TODO:这里没有处理chunk的情况
        long incr = attach.markWriteEnd();
        long complete = attach.getComplete();

        if (incr == -1) {
            // 说明是第一次写
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
