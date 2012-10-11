package com.xunlei.httptool.util;

/**
 * ���������еķ������Ӧ����ֵ
 * 
 * <pre>
 * 0-9 http�ڲ�������
 * 10-19 ���÷�����
 * 
 * @author ����
 * @since 2010-3-6 ����06:02:59
 */
public interface RtnConstants {

    public static final String rtn = "rtn";
    public static final String data = "data";
    public static final String rtnMsg = "rtnMsg";
    /**
     * 0 ��¼̬��֤ͨ��
     */
    public static final int OK = 0;

    /**
     * 9 ������ȡʧ��(���ʽ�Ƿ�,�޴˲�����,��Ӧ IllegalParameterError)
     */
    public static final int PARAM_ILLEGAL = 9;
    /**
     * 10 ��֤����Ч
     */
    public static final int VCODE_INVALID = 10;
    /**
     * 11 ��¼̬��֤ʧ��
     */
    public static final int SESSIONID_INVALID = 11;
    /**
     * 13 ����ҵ����֤��Ч
     */
    public static final int PARAM_INVALID = 13;

    /**
     * 14 ��������ֹ�����������Ƶ����
     */
    public static final int OPERATION_FORBIDDEN = 14;

    /**
     * 500 Ĭ��ϵͳ�ڲ�δ֪����
     */
    public static final int INTERNAL_SERVER_ERROR = 500;
}
