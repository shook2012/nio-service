package com.xunlei.netty.httpserver.util;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.slf4j.Logger;
import com.xunlei.netty.httpserver.exception.ClosedChannelError;
import com.xunlei.netty.httpserver.exception.ProcessFinishedError;
import com.xunlei.util.CloseableHelper;
import com.xunlei.util.Log;

/**
 * @author ZengDong
 * @since 2011-4-15 ����10:13:22
 */
public class NetUtil {

    private static Logger log = Log.getLogger();

    /**
     * У��˿��Ƿ�ռ��,�����ռ�ó����˳�
     */
    public static void checkSocketPortBind(boolean exitWhenError, int... ports) {
        if (System.getProperty("os.name").startsWith("Win")) {
            // ��linux�в����ж��Ƿ�ռ��
            for (int port : ports) {
                if (port > 0) {
                    boolean isBind = true;
                    Socket socket = null;
                    try {
                        socket = new Socket("localhost", port);
                    } catch (ConnectException e1) {
                        isBind = false;
                    } catch (Exception e1) {
                        log.error("", e1);
                        isBind = true;
                    } finally {
                        CloseableHelper.closeSilently(socket);
                    }
                    if (isBind) {
                        String errStr = "Failed to bind to " + port;
                        if (exitWhenError) {
                            System.err.println(errStr);
                            HttpServerConfig.ALARMLOG.error(errStr);
                            System.exit(1);
                        } else {
                            throw new RuntimeException(errStr);
                        }
                    }
                }
            }
        }
    }

    /**
     * У��˿��Ƿ�ռ��,�����ռ�ó����˳�
     */
    public static void checkSocketPortBind(int... ports) {
        checkSocketPortBind(false, ports);
    }

    /**
     * <pre>
     * ���������쳣��
     * Զ������ǿ�ȹر���һ�����е�����,Connection reset by peer
     * 
     * ��֪�����ε��쳣�У�
     * ���������е������ֹ��һ���ѽ���������(windows�п��������������ɵ�)
     * </pre>
     */
    // private String resetByPeerFilterStr = Locale.getDefault().equals(Locale.CHINA) ? "ǿ�ȹر�" : "reset by";
    private static String resetByPeerFilterStr = "reset by";
    private static String resetByPeerFilterStr1 = "ǿ�ȹر�";
    private static String connectionTimedOut = "Connection timed out";

    public static boolean exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        return exceptionCaught(ctx, e, "");
    }

    /**
     * ����ͨ�õ�exception
     */
    public static boolean exceptionCaught(ChannelHandlerContext ctx, Throwable t, String type) throws Exception {
        boolean channelInterrupt = false;
        try {
            Channel channel = ctx.getChannel();
            Object attach = ctx.getAttachment();
            Object v = attach == null ? channel : attach;// �����attach,�ʹ�ӡattach
            if (t != null) {
                if (t instanceof IOException && t.getMessage() != null) {
                    String msg = t.getMessage();
                    if (msg.contains(resetByPeerFilterStr) || msg.contains(resetByPeerFilterStr1)) {
                        log.info("resetByPeer       {}:{},{}", new Object[] { type, v, msg });
                    } else if (msg.contains(connectionTimedOut)) {
                        log.info("connectionTimeout {}:{},{}", new Object[] { type, v, msg });
                    }
                } else if (t instanceof ClosedChannelException || t instanceof ClosedChannelError) {
                    log.info("channelClosed     {}:{},{}", new Object[] { type, v, t.getMessage() });
                    channelInterrupt = true;
                } else if (!(t instanceof ProcessFinishedError)) {
                    log.error("exceptionCaught   {}:{}", new Object[] { type, v, t });
                }
            } else {
                log.error("exceptionCaught  {}:{}", new Object[] { type, v, t });
            }
        } catch (Exception e2) {
        }
        return channelInterrupt;
    }

    /**
     * ����ͨ�õ�exception
     */
    public static boolean exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e, String type) throws Exception {
        try {
            return exceptionCaught(ctx, e.getCause(), type);
        } finally {
            ctx.sendUpstream(e);
        }
    }

}
