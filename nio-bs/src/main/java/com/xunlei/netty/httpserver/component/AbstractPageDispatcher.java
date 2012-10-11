package com.xunlei.netty.httpserver.component;

import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
 * <pre>
 * ���ڼ��㵱ǰurl���䵽�ĸ�ICommand������
 * 
 * SimpleChannelUpstreamHandler��һ���򵥵����̣�
 * 
 * channelOpen
 *  -> channelBound
 *      -> channelConnected
 *      
 *          -> messageReceived
 *              ->ҵ����1
 *          -> writeComplete
 *          
 *          -> messageReceived
 *              ->ҵ����2
 *          -> writeComplete
 *          
 *      -> channelDisconnected
 *  -> channelUnbound
 * channelClosed
 * 
 * ��ʱ��δ֪��״̬�ǣ�
 * childChannelOpen
 * childChannelClosed
 * 
 * channelInterestChanged ��  Invoked when a Channel's interestOps was changed.
 * OP_NONE, OP_READ, OP_WRITE, or OP_READ_WRITE
 * 
 * һ��SimpleChannelUpstreamHandler�� һ�� "����"�ж�Ӧһ��Ψһ�� ChannelHandlerContext,
 * Ҳ�����ؽ�������channel�仯,ctxҲ����
 * 
 * ��ChannelPipeline�е���һ�� channelHandler��Ӧ��������һ�� ctx 
 * ��� ����һ���������,encoder,decoder��Ӧ��ctx�ǲ�ͬ��
 */
public abstract class AbstractPageDispatcher extends SimpleChannelUpstreamHandler {

    public abstract void init();
}
