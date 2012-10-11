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
public class ConfigAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter {// ����һ������Spring���

    private class ConfigEntry {

        private Set<ConfigField> configFields = new LinkedHashSet<ConfigField>(2);
        private boolean guarded;
        private String key;

        public ConfigEntry(String key) {
            this.key = key;
        }

        public synchronized void addField(ConfigField f) {
            // �滻ԭ����configFiled,�Խ�� �ǵ�����bean������������
            configFields.remove(f);
            configFields.add(f);
        }
    }

    private class ConfigField {

        private Object bean; // @Service�����ʵ��
        private Config config;

        private Type[] collectionArgsType;

        private Method collectionInserter; // ���������Ԫ�صķ���
        private Class<?> collectionType; // ʹ�õļ��ϵ�����
        private Field field; // @Service�е�@Config��ǵ��ֶ�

        private boolean resetable; // �Ƿ�����ͨ����������ֵ
        private Method setter; // @Config�ֶε�set����

        private String split; // ����֮��ķָ���
        private String splitKeyValue; // Key-Value֮��ķָ���

        private boolean init;// �Ƿ��ʼ����

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
                } else {// ������Ǽ������ͣ�������ǻ����͵�
                    checkType(type);
                }
                try {
                    this.setter = ReflectConvention.buildSetterMethod(bean.getClass(), field, field.getType());// Ѱ���ֶε�set����
                } catch (Exception e) {
                }
                init = true;
            }
        }

        private void checkGenericType(Class<?> type, Type genericType, int len) {
            if (type == genericType) {// �����ͬ˵����ǰ �����ֶ�,û���õ�����
                throw new ServerConfigError("@Config for [" + field.getName() + "]'s is a " + type.getSimpleName() + ",it must set genericType");
            }
            ParameterizedTypeImpl pt = (ParameterizedTypeImpl) genericType;
            collectionArgsType = pt.getActualTypeArguments();
            for (int i = 0; i < len; i++) {
                if (BeanUtil.isNotBasicType(collectionArgsType[i])) { // ��������,�ڲ��������ͱ����� ��������,��֧��?(WildcardType,WildcardTypeImpl)
                    throw new ServerConfigError("@Config for [" + field.getName() + "]'s field genericType [" + genericType + "] is not supported.");
                }
            }
        }

        /**
         * �ж�Ŀ�������Ƿ��ǻ����ͣ������������׳��쳣
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
            if (resetable || init) { // ֻ������������ĸ�ֵ��resetableΪtrue�ͳ�ʼ����ʱ��
                try {
                    init();
                    boolean isArray = field.getType().isArray();
                    Object realvalue = null;
                    if (collectionType == null) {// ���Ǽ������ͣ������������ʹ���
                        realvalue = typeConverter.convertIfNecessary(value, field.getType());
                    } else {
                        realvalue = collectionType.newInstance();
                        if (Map.class.isAssignableFrom(collectionType)) { // ����Map�����
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
                        } else { // ����Collection�����
                            Class<?> elementType = isArray ? field.getType().getComponentType() : (Class<?>) collectionArgsType[0];
                            for (String str : StringTools.splitAndTrim(value.toString(), split)) {
                                Object elementValue = typeConverter.convertIfNecessary(str, elementType);
                                collectionInserter.invoke(realvalue, elementValue);
                            }
                        }
                    }

                    ReflectionUtils.makeAccessible(field);
                    Object orivalue = ReflectionUtils.getField(field, bean);

                    realvalue = isArray ? typeConverter.convertIfNecessary(realvalue, field.getType()) : realvalue;// ֧�� ԭʼ�������͵� ����
                    String oriValueToString = ArraysUtil.deepToString(orivalue);
                    String realValueToString = ArraysUtil.deepToString(realvalue);
                    if (info != null) {
                        info.append(String.format("ORIVALUE:%-10s ", oriValueToString));
                    }
                    if (!ArraysUtil.deepEquals(orivalue, realvalue)) {
                        if (null == setter) { // Ѱ�Ҹ��ֶε�set�������������֮�������ֱ��ͨ�������ֵ����ȥ
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
    // ��ΪҪ���� �ǵ��������,����Ҫ����ͬ��
    private Map<Method, Object> afterConfigCache = Collections.synchronizedMap(new LinkedHashMap<Method, Object>());// ����������@AfterConfig��Method-Beanӳ��
    private Map<String, ConfigEntry> configCache = Collections.synchronizedMap(new LinkedHashMap<String, ConfigEntry>());// ��������������@Config��ӳ��
    @Autowired
    private ExtendedPropertyPlaceholderConfigurer propertyConfigurer;// �Զ�ע�� ExtendedPropertyPlaceholderConfigurer�������ڻ�ȡ������Դ
    private StringBuilder resetHistory = new StringBuilder(String.format(RESET_HISTORY_FMT, "TIME", "CLASS", "FIELD", "ORIVALUE", "NEWVALUE")); // ����config����ʷ��¼
    private SimpleTypeConverter typeConverter = new SimpleTypeConverter();// ����������ת����

    private void cacheConfigUnit(String key, ConfigField configField) {
        ConfigEntry ce = configCache.get(key);
        if (ce == null) {
            ce = new ConfigEntry(key);
            configCache.put(key, ce);
        }
        ce.addField(configField);
    }

    /**
     * ���@Config�����õ�key���ƣ�Ĭ��Ϊ����������
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
        // ��ֵ���֮����Ҫִ�б�ע��@AfterConfig�ķ���������Ҫ������޲���
        ReflectionUtils.doWithMethods(bean.getClass(), new ReflectionUtils.MethodCallback() {

            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                AfterConfig cfg = method.getAnnotation(AfterConfig.class);
                AfterBootstrap bst = method.getAnnotation(AfterBootstrap.class);
                Object annotation = cfg != null ? cfg : bst; // AfterBootstrap ��AfterConfig���Ӽ�
                String annotationName = cfg != null ? "AfterConfig" : "AfterBootstrap";
                if (annotation != null) {
                    if (Modifier.isStatic(method.getModifiers()) && !Modifier.isFinal(bean.getClass().getModifiers())) {
                        // ע��,����һ��Ҫ������ô�ϸ�
                        // public class AClass {
                        // @AfterConfig
                        // public static void init() {
                        // //���������Aclass��final��,��ô����BClass extend AClass,��Ҳ��Spring��ʼ��ʱ,����ᱻ��������,����ɲ���Ҫ���鷳
                        // //��ΪReflectionUtils.doWithMethods��ReflectionUtils.doWithFieldsʱ,���������и���
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
                        // ��������
                        afterConfigCache.put(method, bean);
                    }
                }
            }
        });
        return true; // ͨ������·���true����
    }

    @Override
    public boolean postProcessAfterInstantiation(final Object bean, String beanName) throws BeansException {
        // ����ע��@Config���ֶθ�ֵ
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {

            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                Config cfg = field.getAnnotation(Config.class);
                if (cfg != null) {
                    if (Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(bean.getClass().getModifiers())) {// ����������ô�ϸ��ԭ����Ϊ�˸�@AfterConfig����Ϊһ��
                        throw new IllegalStateException("@Config annotation on static fields,it's class must be final");
                    }
                    String key = getConfigKey(field);
                    ConfigField configField = new ConfigField(bean, field, cfg);
                    cacheConfigUnit(key, configField);// ��������
                    // ��ʼ��ֵ
                    configField.setValue(propertyConfigurer.getProperty(key), true, false, null);
                }
            }
        }); // ͨ������·���true����
        return true;
    }

    /**
     * �����ǰ��Confi������Ϣ
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
     * ���¼��������ļ�,����ֵ����
     */
    public void reloadConfig(StringBuilder info) {
        try {
            // TODO:������������ж�ʱ����Ļ�,�������
            // ���������ļ�
            Properties props = propertyConfigurer.reload();
            boolean settted = false; // ��ֵ
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
            // ���и�ֵ�������,����@AfterConfig��Ӧ������������
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
