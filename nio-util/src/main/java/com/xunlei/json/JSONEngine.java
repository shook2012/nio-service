package com.xunlei.json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.slf4j.Logger;
import com.xunlei.util.Log;
import com.xunlei.util.ReflectConvention;

/**
 * 配置JSONUtil工作时所使用的两种引擎，JACKSON Engineer优先考虑，如果加载失败就是用自己写的引擎
 * 
 * @author ZengDong
 * @since 2010-6-1 下午11:42:52
 */
public class JSONEngine {

    private static final Logger log = Log.getLogger();
    public static final ObjectMapper DEFAULT_JACKSON_MAPPER;
    public static final ObjectMapper PRETTY_JACKSON_MAPPER;// 打印人更易阅读的Json串

    /**
     * 标记JACKSON的类库是否加载成功
     */
    public static final boolean JACKSON_ENABLE = ReflectConvention.isClassFound("org.codehaus.jackson.map.ObjectMapper", "org.codehaus.jackson.JsonGenerator");
    static {
        if (JACKSON_ENABLE) {
            log.error("JSONUtil Using Jackson Engine...");
            JsonFactory jf = new JsonFactory();
            jf.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);// 放宽jsonParser的限制
            jf.enable(JsonParser.Feature.ALLOW_COMMENTS);
            jf.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
            jf.enable(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
            jf.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);

            DateFormat defaultDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DEFAULT_JACKSON_MAPPER = new ObjectMapper(jf);
            SerializationConfig sc = DEFAULT_JACKSON_MAPPER.getSerializationConfig();
            sc = sc.without(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS).withDateFormat(defaultDF);
            DEFAULT_JACKSON_MAPPER.setSerializationConfig(sc);
            DeserializationConfig dsc = DEFAULT_JACKSON_MAPPER.getDeserializationConfig();
            // 忽略JSON字符串中存在而Java对象实际没有的属性
            dsc = dsc.without(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
            DEFAULT_JACKSON_MAPPER.setDeserializationConfig(dsc);

            PRETTY_JACKSON_MAPPER = new ObjectMapper(jf);
            sc = PRETTY_JACKSON_MAPPER.getSerializationConfig();
            sc = sc.without(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS).withDateFormat(defaultDF).with(SerializationConfig.Feature.INDENT_OUTPUT);
            PRETTY_JACKSON_MAPPER.setSerializationConfig(sc);
            dsc = PRETTY_JACKSON_MAPPER.getDeserializationConfig();
            // 忽略JSON字符串中存在而Java对象实际没有的属性
            dsc = dsc.without(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
            PRETTY_JACKSON_MAPPER.setDeserializationConfig(dsc);
        } else {
            DEFAULT_JACKSON_MAPPER = PRETTY_JACKSON_MAPPER = null;
            log.error("JSONUtil Using InternalJSON Engine...");
        }
    }
}
