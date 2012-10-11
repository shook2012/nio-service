package com.xunlei.spring;

import java.io.IOException;
import java.util.Properties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * 此类主要是负责配置文件的管理，可以重新装载配置文件
 * 
 * @author ZengDong
 */
public class ExtendedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    /**
     * 配置文件
     */
    private Properties props;

    /**
     * 继承自PropertyPlaceholderConfigurer时需要重写的方法，在此方法中可以自定义读取文件的方法
     */
    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties prop) throws BeansException {
        super.processProperties(beanFactory, prop);
        this.props = prop;
    }

    /**
     * 重新加载所有配置
     * 
     * @return
     * @throws IOException
     */
    public Properties reload() throws IOException {
        props = this.mergeProperties();
        return props;
    }

    /**
     * 获得配置文件中某个key对应的值
     * 
     * @param key
     * @return
     */
    public Object getProperty(String key) {
        return props.get(key);
    }
}
