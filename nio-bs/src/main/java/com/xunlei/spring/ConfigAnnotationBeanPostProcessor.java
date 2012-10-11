package com.xunlei.spring;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import com.xunlei.util.ArraysUtil;
import com.xunlei.util.DateStringUtil;
import com.xunlei.util.Log;
import com.xunlei.util.ReflectConvention;
import com.xunlei.util.StringTools;

@Component
public class ConfigAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {// 定义一个匿名Spring组件

    private class ConfigEntry {

        private Set<ConfigField> configFields = new LinkedHashSet<ConfigField>(2);
        private boolean guarded;
        private String key;

        public ConfigEntry(String key) {
            this.key = key;
        }

        public synchronized void addField(ConfigField f) {
            // 替换原来的configFiled,以解决 非单例的bean能正常被回收
            configFields.remove(f);
            configFields.add(f);
        }
    }

    private class ConfigField {

        private Object bean; // @Service对象的实例
        private Config config;

        private Type[] collectionArgsType;

        private Method collectionInserter; // 给集合添加元素的方法
        private Class<?> collectionType; // 使用的集合的类型
        private Field field; // @Service中的@Config标记的字段

        private boolean resetable; // 是否允许通过命令重设值
        private Method setter; // @Config字段的set方法

        private String split; // 属性之间的分隔符
        private String splitKeyValue; // Key-Value之间的分隔符

        private boolean init;// 是否初始化好

        public ConfigField(Object bean, Field field, Config config) {
            this.bean = bean;
            this.field = field;
            this.config = config;
            this.resetable = config.resetable();
            this.split = config.split();
            this.splitKeyValue = config.splitKeyValue();
        }

        private void init() {
            if (!init) {
                Class<?> type = field.getType();
                Type genericType = field.getGenericType();

                if (Collection.class.isAssignableFrom(type)) {// Collection
                    collectionType = isFieldTypeAbstract(type) ? Set.class.isAssignableFrom(type) ? HashSet.class : ArrayList.class : type;
                    checkGenericType(type, genericType, 1);
                    setCollectionInserter(false);
                } else if (Map.class.isAssignableFrom(type)) {// Map
                    collectionType = isFieldTypeAbstract(type) ? HashMap.class : type;
                    checkGenericType(type, genericType, 2);
                    setCollectionInserter(true);
                } else if (type.isArray()) {// Array
                    collectionType = ArrayList.class;
                    checkType(type.getComponentType());
                    setCollectionInserter(false);
                } else {// 如果不是集合类型，则必须是基本型的
                    checkType(type);
                }
                try {
                    this.setter = ReflectConvention.buildSetterMethod(bean.getClass(), field, field.getType());// 寻找字段的set方法
                } catch (Exception e) {
                }
                init = true;
            }
        }

        private void checkGenericType(Class<?> type, Type genericType, int len) {
            if (type == genericType) {// 如果相同说明当前 集合字段,没有用到泛型
                throw new ServerConfigError("@Config for [" + field.getName() + "]'s is a " + type.getSimpleName() + ",it must set genericType");
            }
            ParameterizedTypeImpl pt = (ParameterizedTypeImpl) genericType;
            collectionArgsType = pt.getActualTypeArguments();
            for (int i = 0; i < len; i++) {
                if (BeanUtil.isNotBasicType(collectionArgsType[i])) { // 集合类型,内部参数类型必须是 基本类型,不支持?(WildcardType,WildcardTypeImpl)
                    throw new ServerConfigError("@Config for [" + field.getName() + "]'s field genericType [" + genericType + "] is not supported.");
                }
            }
        }

        /**
         * 判定目标类型是否是基本型，如果不是则会抛出异常
         */
        private void checkType(Class<?> type) {
            if (BeanUtil.isNotBasicType(type)) {
                throw new ServerConfigError("@Config for [" + field.getName() + "]'s field type [" + field.getType().getName() + "] is not supported.");
            }
        }

        private boolean isFieldTypeAbstract(Class<?> type) {
            int mod = type.getModifiers();
            return Modifier.isInterface(mod) || Modifier.isAbstract(mod);
        }

        private void setCollectionInserter(boolean isMap) {
            try {
                collectionInserter = isMap ? collectionType.getMethod("put", Object.class, Object.class) : collectionType.getMethod("add", Object.class);
            } catch (Exception e) {
                throw new ServerConfigError("@Config for [" + field.getName() + "]'s collectionInserter null!  " + e);
            }
        }

        public boolean setValue(Object value, boolean init, boolean tmp, StringBuilder info) {
            if (value == null) {
                return false;
            }
            if (info != null) {
                info.append(String.format("FIELD:%40s.%-40s", bean.getClass().getSimpleName(), field.getName()));
            }
            if (resetable || init) { // 只允许两种情况的赋值：resetable为true和初始化的时候
                try {
                    init();
                    boolean isArray = field.getType().isArray();
                    Object realvalue = null;
                    if (collectionType == null) {// 不是集合类型，按基本型类型处理
                        realvalue = typeConverter.convertIfNecessary(value, field.getType());
                    } else {
                        realvalue = collectionType.newInstance();
                        if (Map.class.isAssignableFrom(collectionType)) { // 处理Map的情况
                            Class<?> keyType = (Class<?>) collectionArgsType[0];
                            Class<?> elementType = (Class<?>) collectionArgsType[1];
                            for (String str : StringTools.splitAndTrim(value.toString(), split)) {
                                List<String> pair = StringTools.splitAndTrim(str, splitKeyValue);
                                if (pair.size() == 2) {
                                    Object keyValue = typeConverter.convertIfNecessary(pair.get(0), keyType);
                                    Object elementValue = typeConverter.convertIfNecessary(pair.get(1), elementType);
                                    collectionInserter.invoke(realvalue, keyValue, elementValue);
                                } else {
                                    log.error("set config:{}.{} MAP FAIL,element[{}] cant find keyvalue by split {}", new Object[] { bean.getClass().getSimpleName(), field.getName(), str,
                                            splitKeyValue });
                                }
                            }
                        } else { // 处理Collection的情况
                            Class<?> elementType = isArray ? field.getType().getComponentType() : (Class<?>) collectionArgsType[0];
                            for (String str : StringTools.splitAndTrim(value.toString(), split)) {
                                Object elementValue = typeConverter.convertIfNecessary(str, elementType);
                                collectionInserter.invoke(realvalue, elementValue);
                            }
                        }
                    }

                    ReflectionUtils.makeAccessible(field);
                    Object orivalue = ReflectionUtils.getField(field, bean);

                    realvalue = isArray ? typeConverter.convertIfNecessary(realvalue, field.getType()) : realvalue;// 支持 原始数据类型的 数组
                    String oriValueToString = ArraysUtil.deepToString(orivalue);
                    String realValueToString = ArraysUtil.deepToString(realvalue);
                    if (info != null) {
                        info.append(String.format("ORIVALUE:%-10s ", oriValueToString));
                    }
                    if (!ArraysUtil.deepEquals(orivalue, realvalue)) {
                        if (null == setter) { // 寻找该字段的set方法，有则调用之，否则就直接通过反射把值赋进去
                            ReflectionUtils.setField(field, bean, realvalue);
                        } else {
                            ReflectionUtils.invokeMethod(setter, bean, new Object[] { realvalue });
                        }
                        String now = DateStringUtil.DEFAULT_DATE_STRING_UTIL.now();
                        if (tmp) {
                            now = now + "*";
                        }
                        resetHistory.append(String.format(RESET_HISTORY_FMT, now, bean.getClass().getSimpleName(), field.getName(), oriValueToString, realValueToString));
                        if (info != null) {
                            info.append("NEWVALUE:").append(realValueToString).append("\n");
                        }
                        // log.warn("set config:{}.{},oriValue:{},value:{},init:{},tmp:{}", new Object[] { bean.getClass().getSimpleName(), field.getName(), orivalue, realvalue, init, tmp });
                        return true;
                    }
                    if (info != null) {
                        info.append("NO CHANGE\n");
                    }
                } catch (Exception e) {
                    log.error("", e);
                    if (info != null) {
                        info.append(e.getClass().getName()).append(":").append(e.getMessage()).append("\n");
                    }
                }
            }
            if (!resetable && info != null) {
                info.append("NO RESETABLE\n");
            }
            return false;
        }

        @Override
        public String toString() {
            ReflectionUtils.makeAccessible(field);
            Object orivalue = ReflectionUtils.getField(field, bean);
            return String.format("%-20s %40s.%-40s %-20s %-10s", ArraysUtil.deepToString(orivalue), bean.getClass().getSimpleName(), field.getName(), resetable ? "RESETABLE" : "",
                    setter != null ? "SETTER" : "");
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((bean == null) ? 0 : bean.getClass().hashCode());
            result = prime * result + ((config == null) ? 0 : config.hashCode());
            result = prime * result + ((field == null) ? 0 : field.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ConfigField other = (ConfigField) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (bean == null) {
                if (other.bean != null) {
                    return false;
                }
            } else if (!bean.getClass().equals(other.bean.getClass())) {
                return false;
            }
            if (config == null) {
                if (other.config != null) {
                    return false;
                }
            } else if (!config.equals(other.config)) {
                return false;
            }
            if (field == null) {
                if (other.field != null) {
                    return false;
                }
            } else if (!field.equals(other.field)) {
                return false;
            }
            return true;
        }

        private ConfigAnnotationBeanPostProcessor getOuterType() {
            return ConfigAnnotationBeanPostProcessor.this;
        }
    }

    private static Logger log = Log.getLogger();
    public static final String RESET_HISTORY_FMT = "%-20s %40s.%-40s %-20s %-20s\n";
    // 因为要处理 非单例的情况,所以要加上同步
    private Map<Method, Object> afterConfigCache = Collections.synchronizedMap(new LinkedHashMap<Method, Object>());// 所有配置了@AfterConfig的Method-Bean映射
    private Map<String, ConfigEntry> configCache = Collections.synchronizedMap(new LinkedHashMap<String, ConfigEntry>());// 缓存所有配置了@Config的映射
    @Autowired
    private ExtendedPropertyPlaceholderConfigurer propertyConfigurer;// 自动注入 ExtendedPropertyPlaceholderConfigurer对象，用于获取配置资源
    private StringBuilder resetHistory = new StringBuilder(String.format(RESET_HISTORY_FMT, "TIME", "CLASS", "FIELD", "ORIVALUE", "NEWVALUE")); // 设置config的历史记录
    private SimpleTypeConverter typeConverter = new SimpleTypeConverter();// 创建简单类型转换器

    private void cacheConfigUnit(String key, ConfigField configField) {
        ConfigEntry ce = configCache.get(key);
        if (ce == null) {
            ce = new ConfigEntry(key);
            configCache.put(key, ce);
        }
        ce.addField(configField);
    }

    /**
     * 获得@Config中设置的key名称，默认为变量的名称
     */
    private String getConfigKey(Field field) {
        Config cfg = field.getAnnotation(Config.class);
        String key = cfg.value().length() <= 0 ? field.getName() : cfg.value();
        return key;
    }

    public StringBuilder getResetHistory() {
        return resetHistory;
    }

    public void postProcessAfterBootstrap(ApplicationContext context) {
        for (String name : context.getBeanDefinitionNames()) {
            Object bean = context.getBean(name);
            postProcessAfterBootstrap(bean, name);
        }
    }

    public boolean postProcessAfterBootstrap(final Object bean, String beanName) throws BeansException {
        // 赋值完成之后，需要执行标注有@AfterConfig的方法，方法要求必须无参数
        ReflectionUtils.doWithMethods(bean.getClass(), new ReflectionUtils.MethodCallback() {

            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                AfterConfig cfg = method.getAnnotation(AfterConfig.class);
                AfterBootstrap bst = method.getAnnotation(AfterBootstrap.class);
                Object annotation = cfg != null ? cfg : bst; // AfterBootstrap 是AfterConfig的子集
                String annotationName = cfg != null ? "AfterConfig" : "AfterBootstrap";
                if (annotation != null) {
                    if (Modifier.isStatic(method.getModifiers()) && !Modifier.isFinal(bean.getClass().getModifiers())) {
                        // 注意,这里一定要限制这么严格
                        // public class AClass {
                        // @AfterConfig
                        // public static void init() {
                        // //如果不限制Aclass是final类,那么当有BClass extend AClass,且也被Spring初始化时,这里会被调用两次,会造成不必要的麻烦
                        // //因为ReflectionUtils.doWithMethods及ReflectionUtils.doWithFields时,会找其所有父类
                        // }
                        // }
                        throw new IllegalStateException("@" + annotationName + " annotation on static methods,it's class must be final");
                    }
                    if (method.getParameterTypes().length != 0) {
                        throw new IllegalAccessError("can't invoke method:" + method.getName() + ",exception:paramters length should be 0");
                    }

                    ReflectionUtils.makeAccessible(method);
                    ReflectionUtils.invokeMethod(method, bean);
                    log.debug("@{} {}.{}", new Object[] { annotationName, bean.getClass().getSimpleName(), method.getName() });
                    if (cfg != null) {
                        // 缓存下来
                        afterConfigCache.put(method, bean);
                    }
                }
            }
        });
        return true; // 通常情况下返回true即可
    }

    @Override
    public boolean postProcessAfterInstantiation(final Object bean, String beanName) throws BeansException {
        // 给标注有@Config的字段赋值
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {

            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                Config cfg = field.getAnnotation(Config.class);
                if (cfg != null) {
                    if (Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(bean.getClass().getModifiers())) {// 这里限制这么严格的原因是为了跟@AfterConfig的行为一致
                        throw new IllegalStateException("@Config annotation on static fields,it's class must be final");
                    }
                    String key = getConfigKey(field);
                    ConfigField configField = new ConfigField(bean, field, cfg);
                    cacheConfigUnit(key, configField);// 缓存下来
                    // 初始化值
                    configField.setValue(propertyConfigurer.getProperty(key), true, false, null);
                }
            }
        }); // 通常情况下返回true即可
        return true;
    }

    /**
     * 输出当前的Confi配置信息
     */
    public StringBuilder printCurrentConfig(StringBuilder sb) {
        sb.append("GUARDED KEY:\n");
        for (Map.Entry<String, ConfigEntry> e : configCache.entrySet()) {
            ConfigEntry ce = e.getValue();
            if (ce.guarded) {
                sb.append(ce.key).append("\n");
            }
        }
        sb.append("\n");
        sb.append("FIELDS:\n");
        for (Map.Entry<String, ConfigEntry> e : configCache.entrySet()) {
            ConfigEntry ce = e.getValue();
            for (ConfigField f : ce.configFields) {
                if (f.resetable) {
                    sb.append(f).append("\n");
                }
            }
        }
        sb.append("\n");
        for (Map.Entry<String, ConfigEntry> e : configCache.entrySet()) {
            ConfigEntry ce = e.getValue();
            for (ConfigField f : ce.configFields) {
                if (!f.resetable) {
                    sb.append(f).append("\n");
                }
            }
        }
        sb.append("\n");
        return sb;
    }

    public void reloadConfig() {
        reloadConfig(null);
    }

    /**
     * 重新加载配置文件,并赋值重置
     */
    public void reloadConfig(StringBuilder info) {
        try {
            // TODO:这里如果能先判断时间戳的话,就最好了
            // 加载配置文件
            Properties props = propertyConfigurer.reload();
            boolean settted = false; // 赋值
            for (ConfigEntry ce : configCache.values()) {
                if (!ce.guarded) {
                    Object value = props.getProperty(ce.key);
                    if (value == null) {
                        continue;
                    }
                    for (ConfigField configField : ce.configFields) {
                        if (configField.setValue(value, false, false, info)) {
                            settted = true;
                        }
                    }
                }
            }
            if (!settted) {
                return;
            }
            if (info != null) {
                info.append("\nINVOKE METHOD:\n");
            }
            // 在有赋值的情况下,调用@AfterConfig对应方法进行重置
            for (Map.Entry<Method, Object> e : afterConfigCache.entrySet()) {
                ReflectionUtils.makeAccessible(e.getKey());
                ReflectionUtils.invokeMethod(e.getKey(), e.getValue());
                if (info != null) {
                    info.append(e.getValue().getClass().getSimpleName()).append(".").append(e.getKey().getName()).append("()\n");
                }
            }
        } catch (IOException e1) {
            log.error("", e1);
            if (info != null) {
                info.append(e1.getClass().getName()).append(":").append(e1.getMessage()).append("\n");
            }
        }
    }

    public void resetGuradedConfig() {
        resetGuradedConfig(null);
    }

    public void resetGuradedConfig(StringBuilder info) {
        for (ConfigEntry ce : configCache.values()) {
            if (ce.guarded) {
                ce.guarded = false;
                if (info != null) {
                    info.append(ce.key).append("\n");
                }
            }
        }
    }

    public void setFieldValue(String key, String value, StringBuilder info) {
        if (key == null) {
            return;
        }
        ConfigEntry ce = configCache.get(key);
        if (ce != null) {
            if (StringTools.isEmpty(value)) {
                if (info != null) {
                    info.append("unguard [").append(key).append("]\n");
                }
                if (ce.guarded) {
                    String now = DateStringUtil.DEFAULT_DATE_STRING_UTIL.now() + "-";
                    resetHistory.append(String.format(RESET_HISTORY_FMT, now, key, "", "", ""));
                    ce.guarded = false;
                }
                return;
            }
            if (info != null) {
                info.append("setting [").append(key).append("] -> [").append(value).append("]\n");
            }
            ce.guarded = true;
            for (ConfigField cf : ce.configFields) {
                cf.setValue(value, false, true, info);
            }
            if (info != null) {
                info.append("\n");
            }
        } else {
            if (info != null) {
                info.append("nofound [").append(key).append("]\n");
            }
        }
    }
}
