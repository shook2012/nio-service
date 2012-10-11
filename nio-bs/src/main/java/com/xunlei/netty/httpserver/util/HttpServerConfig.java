package com.xunlei.netty.httpserver.util;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.jboss.netty.handler.execution.MemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.util.internal.ExecutorUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xunlei.netty.httpserver.HttpServerPipelineFactory;
import com.xunlei.netty.httpserver.component.XLAccessLogger;
import com.xunlei.netty.httpserver.component.XLHttpResponse.ContentType;
import com.xunlei.netty.httpserver.https.HttpsServerPipelineFactory;
import com.xunlei.spring.AfterConfig;
import com.xunlei.spring.Config;
import com.xunlei.util.Log;
import com.xunlei.util.concurrent.ConcurrentUtil;
import com.xunlei.util.concurrent.NamedThreadFactory;

/**
 * @author ZengDong
 * @since 2010-5-21 ����01:15:12
 */
@Service
public final class HttpServerConfig {

    public static final Logger ALARMLOG = Log.getLogger("alarm.com.xunlei.netty");
    public static final int CORE_PROCESSOR_NUM = Runtime.getRuntime().availableProcessors();
    private static ContentType respInnerContentType = ContentType.json;
    private XLAccessLogger accessLog = new XLAccessLogger();

    @Config
    private int listen_port = 80;
    @Config
    private int https_listen_port = 0;
    @Config
    private int connectTimeoutMillis = 5000;
    @Config
    private int receiveBufferSize = 8192;
    @Config
    private int sendBufferSize = 8192;
    @Config
    private String indexCmdName = "echo";
    @Config
    private String cmdSuffix = "Cmd";
    @Config
    private String cmdDefaultMethod = "process";
    @Config
    public int workerCount = 0;// 0��ʾĬ������,-1��ʾ��cpu�ĸ�����1������ʾ,n�������ʾ��n��worker
    // ҵ����ʹ�õ��̳߳�,�������ڴ��ع���
    // ���2000�߳�,60s�߳�δ���������(���Է�������������,������ԭ�����̣߳���)
    // ���̳߳� �ٶ� ִ�е��� ChannelEventRunnable�Ļ�,������������ڴ�Ϊ100M
    // �ܹ��ڴ������� 1G
    // @Config
    // private int plMaximumPoolSize = CORE_PROCESSOR_NUM * 50;
    @Config
    private long plMaxChannelMemorySize = 100 * 1024 * 1024;// Caused by: java.lang.IllegalStateException: can't be changed after a task is executed
    @Config
    private long plMaxTotalMemorySize = 1024 * 1024 * 1024;// Caused by: java.lang.IllegalStateException: can't be changed after a task is executed
    @Config(resetable = true)
    private int plCorePoolSize = CORE_PROCESSOR_NUM * 50;
    @Config(resetable = true)
    private long plKeepAliveSecond = 60L;
    @Config(resetable = true)
    private String plAddBefore = "pageDispatcher";// pipelineExecutor�̳߳ط�����һ�� channelHandlerǰ,Ϊnullʱ,��ʾ�����̳߳�,Ϊ�մ�ʱ,��ʾ�ŵ���ǰ��
    @Config(resetable = true)
    private boolean plAddDefalter = false;// �Ƿ�����ѹ���߼�
    @Config(resetable = true)
    private static long plAddDefalterContentLen = 1024; // ��Ӧ�� �������ٲŽ���ѹ��
    /** �첽����ʱ��Ĭ�ϴ��������ӳش�С */
    @Config
    private static int asyncPoolSize = 1;

    public static long getPlAddDefalterContentLen() {
        return plAddDefalterContentLen;
    }

    /**
     * �첽����ʱ��Ĭ�ϴ��������ӳش�С
     */
    public static int getAsyncPoolSize() {
        return asyncPoolSize;
    }

    @Config(resetable = true)
    private String respDefaultContentType = "json";
    @Config(resetable = true)
    private int slowThreshold = 1000;// ͳ�����������ֵ,Ĭ��Ϊ 1000ms
    @Config(resetable = true)
    private static int keepAliveTimeout = 2;
    @Config(resetable = true)
    private int toleranceTimeout = 10;// ������Ĭ����Ϊ����10s�ӵ�ҵ��/����/����ʱ�� �� �������,����ֱ�ӱ������ǰ�ر�����
    @Config(resetable = true)
    private boolean debugEnable = true;
    @Config(resetable = true)
    private boolean statEnable = true;
    @Config(resetable = true)
    private boolean logaccessEnable = true;
    @Autowired
    private Statistics default_statistics;
    @Autowired
    private Statistics statistics;
    @Resource(name = "httpServerPipelineFactory")
    private HttpServerPipelineFactory httpServerPipelineFactory;
    @Resource(name = "httpsServerPipelineFactory")
    private HttpsServerPipelineFactory httpsServerPipelineFactory;
    @Config(resetable = true)
    private static int asyncProxyPoolChannelCoreNum = 800;
    @Config(resetable = true)
    private static int asyncProxyPoolChannelSwepperDelaySeconds = 60;

    // ���ȼ��ɸߵ���: boss - worker - pipeline - biz
    /**
     * <pre>
     * ��̨�����̳߳� 
     * ע��:
     * ������̶߳��Ǻ�̨�߳�,���û�����߳�,����������˳�
     * 
     * ��ĳɵ���ConcurrentUtil.getDaemonExecutor();
     */
    @Deprecated
    public static ScheduledExecutorService daemonTaskExecutor = ConcurrentUtil.getDaemonExecutor();
    public static final ExecutorService bossExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("New I/O server boss $", Thread.MAX_PRIORITY));
    public static final ExecutorService workerExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("New I/O server worker $", Thread.NORM_PRIORITY + 4));

    /**
     * �й�OrderedMemoryAwareThreadPoolExecutor��MemoryAwareThreadPoolExecutor
     * 
     * <pre>
     * http://www.blogjava.net/hankchen/archive/2012/04/08/373572.html
     * 
     * ����ExecutionHandler��Ҫ���̳߳�ģ�ͣ�Netty�ṩ�����ֿ�ѡ��
     * 
     * 1�� MemoryAwareThreadPoolExecutor ͨ�����̳߳��ڴ��ʹ�ÿ��ƣ��ɿ���Executor�д�������������ޣ���������ʱ���������������񽫱������������ɿ��Ƶ���Channel��������������ޣ���ֹ�ڴ��������
     * 
     * 2�� OrderedMemoryAwareThreadPoolExecutor �� MemoryAwareThreadPoolExecutor �����ࡣ����MemoryAwareThreadPoolExecutor �Ĺ���֮�⣬�������Ա�֤ͬһChannel�д�����¼�����˳���ԣ�����Ҫ�ǿ����¼����첽����ģʽ�¿��ܳ��ֵĴ�����¼�˳�򣬵���������֤ͬһChannel�е��¼�����һ���߳���ִ�У�ͨ��Ҳû��Ҫ����
     * 
     * ���磺
     * 
     * Thread X: --- Channel A (Event A1) --.   .-- Channel B (Event B2) --- Channel B (Event B3) --->
     *                                       \ /
     *                                        X
     *                                       / \
     * Thread Y: --- Channel B (Event B1) --'   '-- Channel A (Event A2) --- Channel A (Event A3) --->
     * ��ͼ������˼�м�����
     * 
     * ��1���������̳߳ض��ԣ�����ͬһ��Channel���¼��������ǰ���˳��������ġ����磬�����ȴ�����Channel A (Event A1) ���ٴ���Channel A (Event A2)��Channel A (Event A3)
     * 
     * ��2��ͬһ��Channel�Ķ���¼�����ֲ����̳߳صĶ���߳���ȥ����
     * 
     * ��3����ͬChannel���¼�����ͬʱ�����ֵ�������̣߳�������Ӱ�졣  
     * 
     * OrderedMemoryAwareThreadPoolExecutor �������¼�������������������ģ���Ϊͨ������£������Ͷ�ϣ���������ܹ�����˳�����Լ��������ر�����Ҫ������ֵ�Ӧ�ò�Э�顣���磺XMPPЭ�顣
     * 
     * ���ڻص�����ҵ�������������������֤����Ҳʹ����OrderedMemoryAwareThreadPoolExecutor��
     * ��֤���������һ��������ʹ�ó����ӣ����ϴ�����������һ������������֤����
     * ͨ�ŵ����ݰ�����С��һ�㶼��200���ֽ����ڡ�һ������£�����������̺ܿ죬����û��ʲô���⡣
     * ���ǣ�������֤������Ҫ���õ������Ľӿڣ�����������ӿڳ����ӳ٣�������������̱�����
     * һ��һ���¼������꣬����Ҫ�����¼�����������ԣ������¼���ȫ�������ˣ�
     * ��������֮����û�����⣬����Ϊ������һ��Channel��һ���������ݰ���������Channel�͹ر��ˣ�����������˳������⣬
     * ������ҵ������Ѹ���յ�����ֻ������ͬ����ԭ�򣨵������ӿڣ�������ʱ���Ƚϳ���
     * ��ʵ����֤���̶��Ƕ������������ݰ��������ʺţ���ÿ���������ݰ�֮����û���κι�ϵ�ģ�����������˳��û�����壡
     */
    private MemoryAwareThreadPoolExecutor pipelineExecutorUnordered;
    private MemoryAwareThreadPoolExecutor pipelineExecutorOrdered;

    @AfterConfig
    public void initPipelineExecutorOrdered() {
        if (pipelineExecutorOrdered == null) {
            pipelineExecutorOrdered = new OrderedMemoryAwareThreadPoolExecutor(plCorePoolSize, plMaxChannelMemorySize, plMaxTotalMemorySize, plKeepAliveSecond, TimeUnit.SECONDS,
                    new NamedThreadFactory("PIPELINE_ORD#", Thread.NORM_PRIORITY + 2));
            // pipelineExecutor.allowCoreThreadTimeOut(false);
        } else {
            pipelineExecutorOrdered.setCorePoolSize(plCorePoolSize);
            pipelineExecutorOrdered.setMaximumPoolSize(plCorePoolSize);
            pipelineExecutorOrdered.setKeepAliveTime(plKeepAliveSecond, TimeUnit.SECONDS);
        }
    }

    // MemoryAwareThreadPoolExecutor�ڲ�ֻʹ��CorePoolSize,����core�߳��ǿ��Ի��յ�(allowCoreThreadTimeOut java1.5ʵ��),Ȼ����������һ������������ LinkedTransferQueue<Runnable>()
    // ��������Plicy��new NewThreadRunsPolicy()��������Զ�������,������ʱ���Ҫ���ڴ�������ж�Ӧ����ô����
    @AfterConfig
    public void initPipelineExecutorUnordered() {
        if (pipelineExecutorUnordered == null) {
            pipelineExecutorUnordered = new MemoryAwareThreadPoolExecutor(plCorePoolSize, plMaxChannelMemorySize, plMaxTotalMemorySize, plKeepAliveSecond, TimeUnit.SECONDS, new NamedThreadFactory(
                    "PIPELINE#", Thread.NORM_PRIORITY + 2));
            // pipelineExecutor.allowCoreThreadTimeOut(false);
        } else {
            pipelineExecutorUnordered.setCorePoolSize(plCorePoolSize);
            pipelineExecutorUnordered.setMaximumPoolSize(plCorePoolSize);
            pipelineExecutorUnordered.setKeepAliveTime(plKeepAliveSecond, TimeUnit.SECONDS);
        }
    }

    @AfterConfig
    public void initRespInnerContentType() {
        if ("xml".equalsIgnoreCase(respDefaultContentType)) {
            respInnerContentType = ContentType.xml;
        } else if ("html".equalsIgnoreCase(respDefaultContentType)) {
            respInnerContentType = ContentType.html;
        } else if ("plain".equalsIgnoreCase(respDefaultContentType)) {
            respInnerContentType = ContentType.plain;
        } else {
            respInnerContentType = ContentType.json;
        }
    }

    @AfterConfig
    private void initStat() {
        statistics = statEnable ? default_statistics : NOPStatistics.INSTANCE;
    }

    public OrderedMemoryAwareThreadPoolExecutor getPipelineExecutor() {
        return (OrderedMemoryAwareThreadPoolExecutor) getPipelineExecutorOrdered();
    }

    public MemoryAwareThreadPoolExecutor getPipelineExecutorUnordered() {
        if (null == pipelineExecutorUnordered) {// ע��˴��ĵ�����Ҫ����AfterConfig��������˳��
            initPipelineExecutorUnordered();
        }
        return pipelineExecutorUnordered;
    }

    public MemoryAwareThreadPoolExecutor getPipelineExecutorOrdered() {
        if (null == pipelineExecutorOrdered) {// ע��˴��ĵ�����Ҫ����AfterConfig��������˳��
            initPipelineExecutorOrdered();
        }
        return pipelineExecutorOrdered;
    }

    // public static final ThreadPoolExecutor bizExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new
    // SynchronousQueue<Runnable>(), new NamedThreadFactory("BIZ#"));

    // Executors.newCachedThreadPool(new NamedThreadFactory("   BizProcessor #"));
    // new ThreadPoolExecutor(10, 100, 65L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    public void setPlCorePoolSize(int plCorePoolSize) {
        this.plCorePoolSize = plCorePoolSize;
        initPipelineExecutorOrdered();
        initPipelineExecutorUnordered();
    }

    public void setPlKeepAliveSecond(long plKeepAliveSecond) {
        this.plKeepAliveSecond = plKeepAliveSecond;
        initPipelineExecutorOrdered();
        initPipelineExecutorUnordered();
    }

    public int getRealWorkerCount() {
        if (workerCount < 0) {
            return CORE_PROCESSOR_NUM * workerCount * -1;
        } else if (workerCount == 0) {
            return CORE_PROCESSOR_NUM * 2;// Ĭ������,Ҳ�͵�ͬ�� -2
        } else
            return workerCount;
    }

    public int getListen_port() {
        return listen_port;
    }

    public int getHttps_listen_port() {
        return https_listen_port;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public int getSlowThreshold() {
        return slowThreshold;
    }

    public static int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public int getToleranceTimeout() {
        return toleranceTimeout;
    }

    public boolean isDebugEnable() {
        return debugEnable;
    }

    public boolean isStatEnable() {
        return statEnable;
    }

    public String getIndexCmdName() {
        return indexCmdName;
    }

    public String getCmdSuffix() {
        return cmdSuffix;
    }

    public String getCmdDefaultMethod() {
        return cmdDefaultMethod;
    }

    public String getPlAddBefore() {
        return plAddBefore;
    }

    public boolean isPlAddDefalter() {
        return plAddDefalter;
    }

    public static ContentType getRespInnerContentType() {
        return respInnerContentType;
    }

    public void setRespDefaultContentType(String respDefaultContentType) {
        this.respDefaultContentType = respDefaultContentType;
        initRespInnerContentType();
    }

    public void setStatEnable(boolean statEnable) {
        this.statEnable = statEnable;
        initStat();
        httpServerPipelineFactory.rebuildPipeline();
        httpsServerPipelineFactory.rebuildPipeline();
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setLogaccessEnable(boolean logaccessEnable) {
        this.logaccessEnable = logaccessEnable;
        initAccessLogger();
    }

    @AfterConfig
    private void initAccessLogger() {
        accessLog.setLogEanble(logaccessEnable);
    }

    public XLAccessLogger getAccessLog() {
        return accessLog;
    }

    public void setPlAddBefore(String plAddBefore) {
        this.plAddBefore = plAddBefore;
        httpServerPipelineFactory.rebuildPipeline();
        httpsServerPipelineFactory.rebuildPipeline();
    }

    public void setPlAddDefalter(boolean plAddDefalter) {
        this.plAddDefalter = plAddDefalter;
        httpServerPipelineFactory.rebuildPipeline();
        httpsServerPipelineFactory.rebuildPipeline();
    }

    /**
     * <pre>
     * �ر�httpServer�ڲ�netty��boss�̸߳�worker�߳�
     * ���ԣ� org.jboss.netty.channel.socket.nio.releaseExternalResources
     * 
     * ע�⣺ windows�������Է���, ���������,����ExecutorUtil.terminate����,������cpuռ��100%,���ȴ��ܾõ����,�����ǻ���50����
     * 
     * ����1�����Է���es.shutdownNow()�������������cpu100%��ԭ��;
     * 
     * ����2�����Է������ʹ��es.shutdown()����(Ҳ���� ���û�е���shutdownNow�����ɹ��������,es.awaitTermination(100, TimeUnit.MILLISECONDS)��Զ�᷵��false
     * ������������shutdown()����,�ڲ�for(;;)�޷��˳�,��ѭ��
     * 
     * [��ϸ�رչ�����鿴ExecutorUtil.terminateԴ��]
     * 
     * netty�ٷ���̳Ҳ���������������?!��
     * http://www.jboss.org/netty/community.html#nabble-td5492010
     * http://www.jboss.org/netty/community.html#nabble-td5976446
     * 
     * https://issues.jboss.org/browse/NETTY-366
     * https://issues.jboss.org/browse/NETTY-380
     */
    public static void releaseExternalResources() {
        ExecutorUtil.terminate(new Executor[] { bossExecutor, workerExecutor });
    }

    public static int getAsyncProxyPoolChannelCoreNum() {
        return asyncProxyPoolChannelCoreNum;
    }

    public static int getAsyncProxyPoolChannelSwepperDelaySeconds() {
        return asyncProxyPoolChannelSwepperDelaySeconds;
    }

    // public void setPlMaximumPoolSize(int plMaximumPoolSize) {
    // this.plMaximumPoolSize = plMaximumPoolSize;
    // pipelineExecutor.setMaximumPoolSize(plMaximumPoolSize);
    // }
}
