package com.xunlei.spring;

/**
 * ʹ��@Config���ó����Զ����һ��Error������
 * 
 * @since 2010-10-31
 * @author hujiachao
 */
public class ServerConfigError extends Error {

    private static final long serialVersionUID = 20101031220420L;
    /**
     * ������Ϣ
     */
    private String errorMessage;

    /**
     * ���췽��
     * 
     * @param errorMessage
     */
    public ServerConfigError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * ��ô�����Ϣ
     */
    @Override
    public String getMessage() {
        return errorMessage;
    }
}
