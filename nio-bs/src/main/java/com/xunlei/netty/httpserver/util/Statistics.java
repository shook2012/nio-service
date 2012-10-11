package com.xunlei.netty.httpserver.util;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.WriteCompletionEvent;
import com.xunlei.netty.httpserver.component.XLContextAttachment;

/**
 * @author ZengDong
 * @since 2010-5-20 ����03:34:28
 */
public interface Statistics {

    /**
     * �ϱ���ͨ��
     */
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e);

    /**
     * �ϱ���ʼ������Ϣ,ͳ�ƽ������ݵ�����
     */
    public void messageReceiving(MessageEvent e);

    /**
     * �ϱ�������Ϣ���,ͳ�ƽ�����ʱ
     */
    public void messageReceived(XLContextAttachment attach);

    /**
     * �ϱ���ʼ������Ϣ,ͳ��ҵ����ʱ
     */
    public void writeBegin(XLContextAttachment attach);

    /**
     * �ϱ�������Ϣ���,ͳ�Ʊ�����ʱ
     */
    public void writeComplete(XLContextAttachment attach, WriteCompletionEvent e);

    /**
     * �ϱ�������,����ͨ������ǰ�ر�
     */
    public void channelInterruptClosed(ChannelHandlerContext ctx);

    /**
     * �ϱ��ر�ͨ��
     */
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e);

}
