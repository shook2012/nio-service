package com.xunlei.netty.httpserver.component;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.springframework.stereotype.Service;

/**
 * ��httpserver�ر�ʱ,��������������
 */
@Service
public class NOPDispatcher extends SimpleChannelUpstreamHandler {

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        ctx.getChannel().close();// ֱ�ӹر�����
    }
}
