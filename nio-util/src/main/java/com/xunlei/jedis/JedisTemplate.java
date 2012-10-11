package com.xunlei.jedis;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.SortingParams;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.exceptions.JedisException;
import com.xunlei.util.Log;
import com.xunlei.util.StringTools;

public class JedisTemplate implements JedisCommands {

    private static final Logger log = Log.getLogger();

    private JedisPool jedisPool;

    public Jedis getJedis() {
        Jedis j = jedisPool.getResource();
        if (!j.isConnected()) {
            log.info("try reconnect jedis:{}", j);
            j.connect();
        }
        return j;
    }

    public JedisPool getJedisPool() {
        return this.jedisPool;
    }

    public void returnResource(Jedis jedis, Exception e) {
        if (e == null) {
            jedisPool.returnResource(jedis);
            throw new JedisException(e);
        } else {
            jedisPool.returnBrokenResource(jedis);
        }
    }

    public JedisTemplate(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public String set(String key, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.set(key, value);
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

    @Override
    public String get(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.get(key);
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

    @Override
    public Boolean exists(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.exists(key);
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

    @Override
    public String type(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.type(key);
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

    @Override
    public Long expire(String key, int seconds) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.expire(key, seconds);
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

    @Override
    public Long expireAt(String key, long unixTime) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.expireAt(key, unixTime);
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

    @Override
    public Long ttl(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.ttl(key);
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

    @Override
    public boolean setbit(String key, long offset, boolean value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.setbit(key, offset, value);
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

    @Override
    public boolean getbit(String key, long offset) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.getbit(key, offset);
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

    @Override
    public long setrange(String key, long offset, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.setrange(key, offset, value);
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

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.getrange(key, startOffset, endOffset);
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

    @Override
    public String getSet(String key, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.getSet(key, value);
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

    @Override
    public Long setnx(String key, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.setnx(key, value);
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

    @Override
    public String setex(String key, int seconds, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.setex(key, seconds, value);
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

    @Override
    public Long decrBy(String key, long integer) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.decrBy(key, integer);
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

    @Override
    public Long decr(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.decr(key);
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

    @Override
    public Long incrBy(String key, long integer) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.incrBy(key, integer);
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

    @Override
    public Long incr(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.incr(key);
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

    @Override
    public Long append(String key, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.append(key, value);
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

    @Override
    public String substr(String key, int start, int end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.substr(key, start, end);
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

    @Override
    public Long hset(String key, String field, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hset(key, field, value);
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

    @Override
    public String hget(String key, String field) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hget(key, field);
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

    @Override
    public Long hsetnx(String key, String field, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hsetnx(key, field, value);
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

    @Override
    public String hmset(String key, Map<String, String> hash) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hmset(key, hash);
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

    @Override
    public List<String> hmget(String key, String... fields) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hmget(key, fields);
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

    @Override
    public Long hincrBy(String key, String field, long value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hincrBy(key, field, value);
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

    @Override
    public Boolean hexists(String key, String field) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hexists(key, field);
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

    @Override
    public Long hdel(String key, String field) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hdel(key, field);
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

    @Override
    public Long hlen(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hlen(key);
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

    @Override
    public Set<String> hkeys(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hkeys(key);
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

    @Override
    public List<String> hvals(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hvals(key);
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

    @Override
    public Map<String, String> hgetAll(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.hgetAll(key);
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

    @Override
    public Long rpush(String key, String string) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.rpush(key, string);
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

    @Override
    public Long lpush(String key, String string) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.lpush(key, string);
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

    @Override
    public Long llen(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.llen(key);
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

    @Override
    public List<String> lrange(String key, long start, long end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.lrange(key, start, end);
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

    @Override
    public String ltrim(String key, long start, long end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.ltrim(key, start, end);
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

    @Override
    public String lindex(String key, long index) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.lindex(key, index);
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

    @Override
    public String lset(String key, long index, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.lset(key, index, value);
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

    @Override
    public Long lrem(String key, long count, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.lrem(key, count, value);
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

    @Override
    public String lpop(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.lpop(key);
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

    @Override
    public String rpop(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.rpop(key);
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

    @Override
    public Long sadd(String key, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.sadd(key, member);
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

    @Override
    public Set<String> smembers(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.smembers(key);
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

    @Override
    public Long srem(String key, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.srem(key, member);
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

    @Override
    public String spop(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.spop(key);
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

    @Override
    public Long scard(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.scard(key);
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

    @Override
    public Boolean sismember(String key, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.sismember(key, member);
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

    @Override
    public String srandmember(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.srandmember(key);
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

    @Override
    public Long zadd(String key, double score, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zadd(key, score, member);
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

    @Override
    public Set<String> zrange(String key, int start, int end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrange(key, start, end);
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

    @Override
    public Long zrem(String key, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrem(key, member);
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

    @Override
    public Double zincrby(String key, double score, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zincrby(key, score, member);
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

    @Override
    public Long zrank(String key, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrank(key, member);
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

    @Override
    public Long zrevrank(String key, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrank(key, member);
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

    @Override
    public Set<String> zrevrange(String key, int start, int end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrange(key, start, end);
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

    @Override
    public Set<Tuple> zrangeWithScores(String key, int start, int end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeWithScores(key, start, end);
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

    @Override
    public Set<Tuple> zrevrangeWithScores(String key, int start, int end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrangeWithScores(key, start, end);
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

    @Override
    public Long zcard(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zcard(key);
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

    @Override
    public Double zscore(String key, String member) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zscore(key, member);
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

    @Override
    public List<String> sort(String key) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.sort(key);
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

    @Override
    public List<String> sort(String key, SortingParams sortingParameters) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.sort(key, sortingParameters);
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

    @Override
    public Long zcount(String key, double min, double max) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zcount(key, min, max);
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

    @Override
    public Set<String> zrangeByScore(String key, double min, double max) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeByScore(key, min, max);
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

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrangeByScore(key, max, min);
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

    @Override
    public Set<String> zrangeByScore(String key, double min, double max, int offset, int count) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeByScore(key, min, max, offset, count);
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

    @Override
    public Set<String> zrevrangeByScore(String key, double max, double min, int offset, int count) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrangeByScore(key, max, min, offset, count);
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

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeByScoreWithScores(key, min, max);
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

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrangeByScoreWithScores(key, max, min);
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

    @Override
    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
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

    @Override
    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max, double min, int offset, int count) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
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

    @Override
    public Long zremrangeByRank(String key, int start, int end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zremrangeByRank(key, start, end);
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

    @Override
    public Long zremrangeByScore(String key, double start, double end) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.zremrangeByScore(key, start, end);
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

    @Override
    public Long linsert(String key, Client.LIST_POSITION where, String pivot, String value) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.linsert(key, where, pivot, value);
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

    public Set<String> keys(String pattern) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.keys(pattern);
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

    public Long del(String keys) {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.del(keys);
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

    public String info() {
        Exception ex = null;
        Jedis jedis = getJedis();
        try {
            return jedis.info();
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

    public Map<String, String> infoMap() {
        String info = info();
        Map<String, String> map = new LinkedHashMap<String, String>();
        for (String line : StringTools.splitAndTrim(info, "\n")) {
            String[] arr = StringTools.splitAndTrimAsArray(line, ":");
            try {
                map.put(arr[0], arr[1]);
            } catch (Exception e) {
            }
        }
        return map;
    }

    // public static class RedisInfo {
    //
    // public String redis_version;
    // public String redis_git_sha1;
    // public int redis_git_dirty;
    // public int arch_bits;
    // public String multiplexing_api;
    // public String gcc_version;
    // public int process_id;
    // public int uptime_in_seconds;
    // public int uptime_in_days;
    // public int lru_clock;
    // public float used_cpu_sys;
    // public float used_cpu_user;
    // public float used_cpu_sys_children;
    // public float used_cpu_user_children;
    // public int connected_clients;
    // public int connected_slaves;
    // public int client_longest_output_list;
    // public int client_biggest_input_buf;
    // public int blocked_clients;
    // public int used_memory;
    // public String used_memory_human;
    // public int used_memory_rss;
    // public int used_memory_peak;
    // public String used_memory_peak_human;
    // public float mem_fragmentation_ratio;
    // public String mem_allocator;
    // public int loading;
    // public int aof_enabled;
    // public int changes_since_last_save;
    // public int bgsave_in_progress;
    // public int last_save_time;
    // public int bgrewriteaof_in_progress;
    // public int total_connections_received;
    // public int total_commands_processed;
    // public int expired_keys;
    // public int evicted_keys;
    // public int keyspace_hits;
    // public int keyspace_misses;
    // public int pubsub_channels;
    // public int pubsub_patterns;
    // public int latest_fork_usec;
    // public int vm_enabled;
    // public String role;
    // public String db0;
    // }
}
