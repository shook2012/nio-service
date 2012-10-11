package com.xunlei.spring;

/**
 * 使用@Config配置出错，自定义的一个Error的子类
 * 
 * @since 2010-10-31
 * @author hujiachao
 */
public class ServerConfigError extends Error {

    private static final long serialVersionUID = 20101031220420L;
    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 构造方法
     * 
     * @param errorMessage
     */
    public ServerConfigError(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * 获得错误信息
     */
    @Override
    public String getMessage() {
        return errorMessage;
    }
}
