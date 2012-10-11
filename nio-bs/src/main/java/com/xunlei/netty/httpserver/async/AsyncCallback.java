package com.xunlei.netty.httpserver.async;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import com.xunlei.netty.httpserver.component.XLContextAttachment;

/**
 * ���첽���proxy��������ݺ�Ļص�������
 * 
 * @author ZengDong
 * @since 2011-10-7 ����10:01:48
 */
public interface AsyncCallback {

    public static abstract class AsyncCallbackAgg implements AsyncCallback {

        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e, XLContextAttachment attach) throws Exception {
            if (attach.asyncMessageEventReceived(e)) {
                // ������Ϣ���ۺ���ɣ�����Խ�����һ������
                allMessageReceived(attach);
            }
        }

        public abstract void allMessageReceived(XLContextAttachment attach) throws Exception;
    }

    public abstract void messageReceived(ChannelHandlerContext ctx, MessageEvent e, XLContextAttachment attach) throws Exception;

    /**
     * <pre>
     * ���ҵ���channel��Ӧ��attach����£������º���
     * 
     * 
     * ע��
     * �����ChannelEvent�п�����ExceptionEvent��Ӧ����ProxyClient������� ���� MessageEvent��Ӧ����ҵ�����
     * 
     * ֱ�Ӹĳɴ�throwable
     */
    public abstract void exceptionCaught(ChannelHandlerContext ctx, Throwable e, XLContextAttachment attach) throws Exception;

}