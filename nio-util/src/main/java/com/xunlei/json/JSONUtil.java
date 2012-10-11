package com.xunlei.json;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import com.xunlei.util.Log;

/**
 * ������ת����JSON��ʽ�Ĺ����࣬���JACKSON Engineer���سɹ�������ֱ��ת��������ʹ��JSONEncoderת��
 * 
 * @author ZengDong
 * @since 2010-6-1 ����11:46:55
 */
public class JSONUtil {

    private static final Logger log = Log.getLogger();

    /**
     * ��Object���͵Ķ���ת����JSON
     */
    public static String fromObject(Object obj) {
        return fromObject(obj, JSONEngine.JACKSON_ENABLE);
    }

    /**
     * ��Object���͵Ķ���ת����JSON������������ʹ�õ�����
     * 
     * @param obj ��ת���Ķ���
     * @param usingJackson �Ƿ�ʹ��JACKSON����
     * @return
     */
    public static String fromObject(Object obj, boolean usingJackson) {
        if (usingJackson) {
            return fromObject(obj, JSONEngine.DEFAULT_JACKSON_MAPPER);
        }
        return JSONEncoder.encode(obj);
    }

    public static String fromObject(Object obj, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("", e);
        }
        return "{}";
    }

    public static String fromObjectPretty(Object obj) {
        return fromObject(obj, JSONEngine.PRETTY_JACKSON_MAPPER);
    }
}
