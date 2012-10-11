package com.xunlei.jedis;

import org.slf4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;
import com.xunlei.util.Log;

public abstract class JedisTemp {

    private static final Logger log = Log.getLogger();

    private JedisPool jedisPool;

    public JedisTemp(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void execute() {
        Jedis jedis = jedisPool.getResource();
        if (!jedis.isConnected()) {
            log.info("try reconnect jedis:{}", jedis);
            jedis.connect();
        }
        Exception ex = null;
        try {
            executeInner(jedis);
        } catch (Exception e) {
            jedisPool.returnBrokenResource(jedis);
            ex = e;
            throw new JedisException(e);
        } finally {
            if (ex == null) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    public abstract void executeInner(Jedis jedis) throws Exception;
}
