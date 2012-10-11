package com.xunlei.spring;

import java.io.IOException;
import java.util.Properties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

/**
 * ������Ҫ�Ǹ��������ļ��Ĺ�����������װ�������ļ�
 * 
 * @author ZengDong
 */
public class ExtendedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    /**
     * �����ļ�
     */
    private Properties props;

    /**
     * �̳���PropertyPlaceholderConfigurerʱ��Ҫ��д�ķ������ڴ˷����п����Զ����ȡ�ļ��ķ���
     */
    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties prop) throws BeansException {
        super.processProperties(beanFactory, prop);
        this.props = prop;
    }

    /**
     * ���¼�����������
     * 
     * @return
     * @throws IOException
     */
    public Properties reload() throws IOException {
        props = this.mergeProperties();
        return props;
    }

    /**
     * ��������ļ���ĳ��key��Ӧ��ֵ
     * 
     * @param key
     * @return
     */
    public Object getProperty(String key) {
        return props.get(key);
    }
}
