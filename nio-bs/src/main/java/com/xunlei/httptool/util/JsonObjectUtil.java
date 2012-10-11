package com.xunlei.httptool.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.xunlei.json.JSONUtil;
import com.xunlei.util.MapUtil;

/**
 * Object��JSONת���Ĺ�����
 * 
 * @author ZengDong
 */
public class JsonObjectUtil {

    /**
     * ��ö����JSON��ʾ
     */
    public static String getDataJsonObject(Object data) {
        return JSONUtil.fromObject(data);
    }

    /**
     * ����ȷ������ key,value,����map
     * 
     * @param keyvalue
     * @return Map����
     */
    public static Map<String, Object> buildMap(Object... keyvalue) {
        return MapUtil.buildMap(new LinkedHashMap<String, Object>(keyvalue.length / 2), keyvalue);
    }

    /**
     * ��õ�¼̬��֤ͨ����JSON
     */
    public static String getOnlyOkJson() {
        return "{\"" + RtnConstants.rtn + "\":" + RtnConstants.OK + "}";
    }

    /**
     * ���ָ����rtn��JSON
     */
    public static String getOnlyRtnJson(int rtn) {
        return "{\"" + RtnConstants.rtn + "\":" + rtn + "}";
    }

    /**
     * ֱ����getRtnAndDataJsonObject�Ϳ�����
     */
    @Deprecated
    public static String getRtnAndDataJsonArray(int rtn, List<Map<String, Object>> list) {
        Map<String, Object> object = new LinkedHashMap<String, Object>(2);
        object.put(RtnConstants.rtn, rtn);
        object.put(RtnConstants.data, list);
        return JSONUtil.fromObject(object);
    }

    /**
     * ֱ����getRtnAndDataJsonObject�Ϳ�����
     */
    // @Deprecated
    // public static String getRtnAndDataJsonObject(int rtn, Map<String, Object> data) {
    // Map<String, Object> object = new LinkedHashMap<String, Object>(2);
    // object.put(RtnConstants.rtn, rtn);
    // object.put(RtnConstants.data, data);
    // return JSONUtil.fromObject(object);
    // }

    /**
     * ���ָ����rtn��ָ����data��JSON
     */
    public static String getRtnAndDataJsonObject(int rtn, Object data) {
        Map<String, Object> object = new LinkedHashMap<String, Object>(2);
        object.put(RtnConstants.rtn, rtn);
        object.put(RtnConstants.data, data);
        return JSONUtil.fromObject(object);
    }

    /**
     * ���ָ��rtn��rtnMsg��data��JSON
     * 
     * @param rtn ������
     * @param rtnMsg ��ǰ��չʾ��ʾ����ı���Ϣ
     * @param data ����
     * @return JSON��ʾ
     */
    public static String getRtnAndDataJsonObject(int rtn, String rtnMsg, Object data) {
        Map<String, Object> object = new LinkedHashMap<String, Object>(2);
        object.put(RtnConstants.rtn, rtn);
        object.put(RtnConstants.rtnMsg, rtnMsg);
        object.put(RtnConstants.data, data);
        return JSONUtil.fromObject(object);
    }
}
