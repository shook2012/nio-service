///**
// * 
// */
//package com.kewen.monitor.stat;
//
//import org.jboss.netty.channel.socket.nio.NioWorkerStat;
//import com.xunlei.netty.httpserver.Bootstrap;
//
//
///**
// * @author dongyansheng
// * @since 2012-10-11  下午2:47:01
// */
//public class BootstrapStatic extends Bootstrap {
//
//    /* (non-Javadoc)
//     * @see com.xunlei.netty.httpserver.Bootstrap#channelstatic()
//     */
//    @Override
//    public void channelstatic() {
//        // TODO Auto-generated method stub
//        try {
//            NioWorkerStat.registerNioWorkers(serverSocketChannelFactory);// 注册到NioWorkerStat,用于统计
//        } catch (Throwable e) {
//            log.warn("{}:{}", e.getClass().getSimpleName(), e.getMessage());
//        }
//    }
//
//    
//    
//}
