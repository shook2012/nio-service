package com.xunlei.httptool.util;

/**
 * 定义了所有的返回码对应的数值
 * 
 * <pre>
 * 0-9 http内部返回码
 * 10-19 常用返回码
 * 
 * @author 曾东
 * @since 2010-3-6 下午06:02:59
 */
public interface RtnConstants {

    public static final String rtn = "rtn";
    public static final String data = "data";
    public static final String rtnMsg = "rtnMsg";
    /**
     * 0 登录态验证通过
     */
    public static final int OK = 0;

    /**
     * 9 参数获取失败(如格式非法,无此参数等,对应 IllegalParameterError)
     */
    public static final int PARAM_ILLEGAL = 9;
    /**
     * 10 验证码无效
     */
    public static final int VCODE_INVALID = 10;
    /**
     * 11 登录态验证失败
     */
    public static final int SESSIONID_INVALID = 11;
    /**
     * 13 参数业务验证无效
     */
    public static final int PARAM_INVALID = 13;

    /**
     * 14 操作被禁止，如请求过于频繁等
     */
    public static final int OPERATION_FORBIDDEN = 14;

    /**
     * 500 默认系统内部未知错误
     */
    public static final int INTERNAL_SERVER_ERROR = 500;
}
