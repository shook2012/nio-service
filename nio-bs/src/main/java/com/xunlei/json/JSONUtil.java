package com.xunlei.json;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import com.xunlei.util.Log;

/**
 * 将对象转换成JSON格式的工具类，如果JACKSON Engineer加载成功就用其直接转换，否则使用JSONEncoder转换
 * 
 * @author ZengDong
 * @since 2010-6-1 下午11:46:55
 */
public class JSONUtil {

    private static final Logger log = Log.getLogger();

    /**
     * 将Object类型的对象转换成JSON
     */
    public static String fromObject(Object obj) {
        return fromObject(obj, JSONEngine.JACKSON_ENABLE);
    }

    /**
     * 将Object类型的对象转换成JSON，可是设置所使用的引擎
     * 
     * @param obj 待转换的对象
     * @param usingJackson 是否使用JACKSON引擎
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
