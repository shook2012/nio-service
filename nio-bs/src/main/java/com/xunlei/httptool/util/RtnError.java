package com.xunlei.httptool.util;

/**
 * �������ݵ������࣬Error������
 * 
 * @author ZengDong
 * @since 2010-9-13 ����01:56:01
 */
public abstract class RtnError extends Error {

    private static final long serialVersionUID = 1L;
    /**
     * �������ݵ�ID
     */
    protected int rtn;
    /**
     * ���ݵ�����
     */
    protected String msg;
    /**
     * ���ݵ�json��ʾ
     */
    protected String _json;

    /**
     * Ĭ�Ϲ��췽��
     */
    public RtnError() {
        this.msg = this.getClass().getSimpleName();
    }

    /**
     * ��÷������ݵ�JSON��ʾ
     * 
     * @return
     */
    public String getJson() {
        if (_json == null) {
            _json = JsonObjectUtil.getRtnAndDataJsonObject(getRtn(), JsonObjectUtil.buildMap("msg", getMsg()));
        }
        return _json;
    }

    public abstract int getRtn();

    /**
     * ���ص���������
     * 
     * @return
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Ϊ�������쳣�ʼ��л�ȡ���쳣�ľ�����Ϣ�������˴˷���
     */
    @Override
    public String getMessage() {
        return msg;
    }

    @Override
    public String toString() {
        return "RtnError [rtn=" + rtn + ", msg=" + msg + "]";
    }
}
