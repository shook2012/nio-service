package com.xunlei.command;

import java.io.IOException;

/**
 * 不能分配内存错误处理接口
 * 
 * @author ZengDong
 * @since 2010-10-14 下午01:56:53
 */
public interface CantAllocateMemErrorHandler {

    /**
     * 处理异常的方法
     * 
     * @param e IO异常
     */
    public void handleCantAllocateMemError(IOException e);
}
