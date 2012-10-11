package com.xunlei.netty.httpserver.cmd.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPool;
import com.xunlei.jedis.JedisTemplate;
import com.xunlei.netty.httpserver.cmd.BaseStatCmd;
import com.xunlei.netty.httpserver.cmd.annotation.CmdCategory;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import com.xunlei.spring.AfterBootstrap;
import com.xunlei.spring.BeanUtil;

/**
 * @author 曾东
 * @since 2012-6-18 下午8:05:55
 */
@Service
@CmdCategory("system")
public class RedisCmd extends BaseStatCmd {

    private Map<String, JedisTemplate> jedisTemplateMap;

    @AfterBootstrap
    public void init() {
        Map<String, JedisPool> jedisPoolMap = BeanUtil.getTypedBeans(JedisPool.class);
        Map<String, JedisTemplate> jedisTemplateMap = new HashMap<String, JedisTemplate>();
        for (Entry<String, JedisPool> e : jedisPoolMap.entrySet()) {
            String beanName = e.getKey();
            JedisPool pool = e.getValue();
            jedisTemplateMap.put(beanName, new JedisTemplate(pool));
        }
        this.jedisTemplateMap = jedisTemplateMap;
    }

    public Object stat(XLHttpRequest request, XLHttpResponse response) throws Exception {
        init(request, response);
        StringBuilder info = new StringBuilder();
        // for (Entry<String, JedisTemplate> e : jedisTemplateMap.entrySet()) {
        // String beanName = e.getKey();
        // JedisTemplate t = e.getValue();
        // info.append(beanName).append(":\n");
        // info.append(t.info());
        // info.append("-----------------------------------------\n\n");
        // }
        List<Map<String, String>> infos = new ArrayList<Map<String, String>>(jedisTemplateMap.size());
        Set<String> keys = new LinkedHashSet<String>();
        keys.add("                     ");
        for (Entry<String, JedisTemplate> e : jedisTemplateMap.entrySet()) {
            JedisTemplate t = e.getValue();
            Map<String, String> map = t.infoMap();
            infos.add(map);
            keys.addAll(map.keySet());
        }

        StringBuilder tableHeader = new StringBuilder();
        for (String k : keys) {
            tableHeader.append("%-").append(Math.max(10, k.length())).append("s ");
        }
        String fmt = tableHeader.append("\n").toString();
        info.append(String.format(fmt, keys.toArray())); // 打印表头

        int infosIndex = 0;
        for (Entry<String, JedisTemplate> e : jedisTemplateMap.entrySet()) {
            String beanName = e.getKey();
            Map<String, String> map = infos.get(infosIndex++);
            Object[] args = new String[keys.size()];
            Iterator<String> iterator = keys.iterator();
            iterator.next();
            args[0] = beanName;

            for (int i = 1; iterator.hasNext(); i++) {
                String key = (String) iterator.next();
                String value = map.get(key);
                args[i] = value == null ? "N/A" : value;
            }
            info.append(String.format(fmt, args));
        }
        return info;
    }
}
