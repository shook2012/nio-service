package com.xunlei.command;

import java.io.IOException;

/**
 * ���ܷ����ڴ������ӿ�
 * 
 * @author ZengDong
 * @since 2010-10-14 ����01:56:53
 */
public interface CantAllocateMemErrorHandler {

    /**
     * �����쳣�ķ���
     * 
     * @param e IO�쳣
     */
    public void handleCantAllocateMemError(IOException e);
}
