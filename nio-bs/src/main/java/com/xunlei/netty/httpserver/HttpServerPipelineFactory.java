package com.xunlei.netty.httpserver;

import static org.jboss.netty.channel.Channels.pipeline;
import java.util.NoSuchElementException;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.xunlei.netty.httpserver.component.AbstractPageDispatcher;
import com.xunlei.netty.httpserver.component.NOPDispatcher;
import com.xunlei.netty.httpserver.component.XLHttpChunkAggregator;
import com.xunlei.netty.httpserver.component.XLHttpRequestDecoder;
import com.xunlei.netty.httpserver.component.XLHttpResponseEncoder;
import com.xunlei.netty.httpserver.util.HttpServerConfig;
import com.xunlei.spring.AfterConfig;
import com.xunlei.util.Log;

/**
 * <pre>
 * һ��Boss����һ���˿�,��80�˿�
 * ��һֱ�Ӷ˿���ͨ��selector�õ�OP_ACCEPT������
 * 
 *   SocketChannel acceptedSocket = channel.socket.accept();
 *                     if (acceptedSocket != null) {
 *                         registerAcceptedChannel(acceptedSocket, currentThread);
 *                     }
 * �õ�acceptedSocket��,�������������nextWorker
 * 
 * NioWorker worker = nextWorker();
 *                 worker.register(new NioAcceptedSocketChannel(
 *                         channel.getFactory(), pipeline, channel,
 *                         NioServerSocketPipelineSink.this, acceptedSocket,
 *                         worker, currentThread), null);
 * 
 * (�ӵ���registerTaskQueue��)
 * 
 * selector.wakeup();��ʲô��˼��
 * worker������,��һֱִ����������:
 *   processRegisterTaskQueue();  
 *     --> runһ��RegisterTask(û���ö������߳�,ֱ���� Runnable.run()��ִ�е�)
 *   processWriteTaskQueue();
 *     --> runһ��writeTaskQueue(û���ö������߳�,ֱ���� Runnable.run()��ִ�е�)
 *     --> �ڲ���ִ��nioWorker.writeFromTaskLoop
 *   processSelectedKeys(selector.selectedKeys());
 *     --> �ж�selectedKeys������read�Ļ�,��read(SelectionKey),Ҳ����OP_READ
 *     --> �ж�selectedKeys������write�Ļ�,��writeFromSelectorLoop(k),Ҳ����OP_WRITE
 *      -->���߼��� �ȶ���д,����������д
 * </pre>
 * 
 * @author ZengDong
 * @since 2010-5-25 ����08:46:42
 */
@Component
public class HttpServerPipelineFactory implements ChannelPipelineFactory {

    public class SimpleIdleStateAwareChannelHandler extends IdleStateAwareChannelHandler {

        @Override
        public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
            log.debug("channel:{},idleState:{}", e.getChannel(), e.getState());
            e.getChannel().close();
        }
    }

    private static final Logger log = Log.getLogger();
    @Autowired
    private Bootstrap bootstrap;
    @Autowired
    private HttpServerConfig config;

    // 2012-03-22 HttpMessageEncoder �����д� volatile chunk�����������ǵ��������� nettyHttpServer����һֱû�� chunk��resp�����Բ���
    // TODO:�ر�ע�� ���Ҫʹ�ã� incubation.com.xunlei.netty.httpserver.cmd �е� chunk���ܣ���Ҫ�ر�������
    private HttpResponseEncoder encoder = new XLHttpResponseEncoder();

    // 2012-05-31 ʹ��chunkAggeator���ۺ�chunk��Ϣ��������ԭ����basePageDispatcher��Ū(basePageDispatcher��ҵ��㣬ǰ��������̳߳أ�Ϊ�����ܿ��ǣ��̳߳غ����Handler�����¼��ǲ�����˳���)
    // 2012-05-31 ��ԭ��netty��HttpChunkAggregator �Ż��ɵ�����
    private XLHttpChunkAggregator chunkAggeator = new XLHttpChunkAggregator(1048576);

    private ExecutionHandler executionHandler;
    // ---����Ϊ��ѡ��,δ�����õ� ---
    private IdleStateAwareChannelHandler idleAwareHandler;
    private Timer idleCheckTimer;
    @Autowired
    private NOPDispatcher nopDispatcher;
    @Autowired
    private AbstractPageDispatcher pageDispatcher;

    // ����
    public ChannelPipeline _getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();
        boolean https = false;
        boolean chunks = false;
        boolean idle_check = true;
        if (https) {
            // SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
            // engine.setUseClientMode(false);
            // pipeline.addLast("ssl", new SslHandler(engine));
        }
        pipeline.addLast("decoder", new XLHttpRequestDecoder(config.getStatistics(), config));
        if (chunks) {
            pipeline.addLast("aggregator", new HttpChunkAggregator(1048576));
        }
        pipeline.addLast("encoder", encoder);
        if (idle_check) {
            // pipeline.addLast("idleStateHandler", new IdleStateHandler(getIdleCheckTimer(), config.readerIdleTimeSeconds, config.writerIdleTimeSeconds, config.allIdleTimeSeconds));
            pipeline.addLast("idleAwareHandler", getIdleAwareHandle());
        }
        pipeline.addLast("pageDispatcher", pageDispatcher);
        if (log.isDebugEnabled())
            log.debug("pipeline#{}:{}", pipeline.hashCode(), pipeline);
        return pipeline;
    }

    public IdleStateAwareChannelHandler getIdleAwareHandle() {
        if (idleAwareHandler == null) {
            idleAwareHandler = new SimpleIdleStateAwareChannelHandler();
        }
        return idleAwareHandler;
    }

    public Timer getIdleCheckTimer() {
        if (idleCheckTimer == null) {
            idleCheckTimer = new HashedWheelTimer();
        }
        return idleCheckTimer;
    }

    // @Override
    // public ChannelPipeline getPipeline() throws Exception {
    // ChannelPipeline pipeline = pipeline();
    //
    // for (Map.Entry<String, ChannelHandler> e : currentPipelineMap.entrySet()) {
    // pipeline.addLast(e.getKey(), e.getValue());
    // }
    // pipeline.replace("DECODER", "decoder", new XLHttpRequestDecoder(config.getStatistics(), config));
    // return pipeline;
    // }

    @AfterConfig
    public void init() {
        // 2012-05-31 BasePageDispatcher �Ѹĳ�֧�� MemoryAwareThreadPoolExecutor
        executionHandler = new ExecutionHandler(config.getPipelineExecutorUnordered());
    }

    public void addSslHandler(ChannelPipeline pipeline) {
    }

    /**
     * <pre>
     * 									 Server
     *  				   LAST----------------------------LAST
     *  					^		pageDispatcher			|
     * 						|		idleChannelHandler		|
     * UpStream(��������)	|		encoder					|	DownStream(������Ӧ)
     * 						|		decoder					|
     * 						|		executor				V
     * 					  FIRST---------------------------FIRST
     * 								  	 Client
     * </pre>
     */
    @Override
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();

        if (bootstrap.isStop()) {
            pipeline.addLast("nop", nopDispatcher);// TODO:���ڹر���,��ֱ�Ӽ򵥴����ڲ�ʲô�߼�������
            return pipeline;
        }
        addSslHandler(pipeline);
        // Ҫע��,������˵ ���빤�� ��ô���Ŷ�������ҵ������һ���߳���

        // NioWorker.write0(Channel) ������,���� writeFromSelectorLoop(SelectionKey),writeFromTaskLoop(Channel),writeFromUserCode(Channel) ��������

        // ���executor������firstλ��,�����,����Ĺ���Ҳ����executor�����,�������encoder,decoder����,�������빤������NioRorker����ɵ�,
        // �������nioWorker�����������Ѿ�������,һ��������� cpu������2��,������˳�����������queque��ά����

        // ���Է��� ��executor���� ǰ��,���Լ���nioworker�ı��������,�Ӷ��ܹ��� ������뻥������(��nioworker��ʵ�ֵĻ�,���������,Ҳ�����۵��������,��Ϊ��ʱnioworker�󲿷��ڽ���������,������)

        // ab -k -n 100000 -c 1000 "http://192.168.14.199:8927/test/length?len=10240"
        // ��executor����ɱ�����빤��ʱ,���� �̳߳���queueSize��߷���Դﵽ 969
        pipeline.addLast("decoder", new XLHttpRequestDecoder(config.getStatistics(), config)); // �����ÿ���½�����ʱ,����Ҫ�ٽ�
        pipeline.addLast("aggregator", chunkAggeator);
        pipeline.addLast("encoder", encoder);
        if (config.isPlAddDefalter()) {
            pipeline.addLast("deflater", new HttpContentCompressor());// HttpContentCompressor �����̰߳�ȫ�� // 2012-04-18 ע����������Ϊ��ʵ�ֿ��жϰ��峤�Ȼ�ʵʱ �����Ƿ�gzip���ܣ��������Լ��İ�
        }
        pipeline.addLast("pageDispatcher", pageDispatcher);

        // A. ��NioWorker��������빤��
        // B. ���빤��Ҳ��ExecutionHandler�е��̳߳�������
        String addBefore = config.getPlAddBefore();
        if (addBefore != null) {
            if (addBefore.isEmpty()) {
                pipeline.addFirst("executor", executionHandler); // TODO: 2012-05-31 ��������ע�⣬��Ϊ�����̳߳ظĳ��˲������¼�˳�����Բ�Ҫ���̳߳طŵ� decoder��aggeatorǰ��decoder��aggeator�Ƕ��¼����еģ�
            } else {
                try {
                    pipeline.addBefore(addBefore, "executor", executionHandler);
                } catch (NoSuchElementException e) {// �������ô���
                    log.error("config error,config.plAddBefore:{} NOT FOUND,execption:{}", addBefore, e);
                    pipeline.addFirst("executor", executionHandler);
                }
            }
        }
        // if (log.isDebugEnabled())
        // log.debug("pipeline#{}:{}", pipeline.hashCode(), pipeline);
        return pipeline;
    }

    // private Map<String, ChannelHandler> currentPipelineMap;

    /**
     * �ؽ�pipeline˳��
     */
    public HttpServerPipelineFactory rebuildPipeline() {
        return this;
        // if (bootstrap == null || config == null)
        // return;
        // ChannelPipeline pipeline = initPipeline();
        // Map<String, ChannelHandler> pipelineMap = pipeline.toMap();
        // log.error("CurrentPipelineMap[{}]:{}", pipelineMap.size(), pipelineMap);
        // this.currentPipelineMap = pipelineMap;
    }
}
