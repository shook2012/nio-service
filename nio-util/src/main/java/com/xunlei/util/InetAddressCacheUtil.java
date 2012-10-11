package com.xunlei.util;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import sun.net.InetAddressCachePolicy;

/**
 * @author 曾东
 * @since 2012-4-25 下午12:11:46
 */
public class InetAddressCacheUtil {

    public static class CacheDetail {

        private InetAddress[] address;
        private long expiration;

        public CacheDetail(long expiration, InetAddress[] address) {
            this.expiration = expiration;
            this.address = address;
        }

        public InetAddress[] getAddress() {
            return address;
        }

        public String getExpire() {
            return DateStringUtil.DEFAULT.format(new Date(expiration));
        }

        public String getRemain() {
            if (expiration >= 0) {
                long r = expiration - System.currentTimeMillis();
                if (r >= 0) {// 还没有过期
                    return HumanReadableUtil.timeSpan(r);
                }
                return HumanReadableUtil.timeSpan(r);
            }
            return "FOREVER";
        }

        @Override
        public String toString() {
            return String.format("%s remain:%s expire:%s", Arrays.toString(address), getRemain(), getExpire());
        }
    }

    private static final Object addressCache = _getCache("addressCache");
    private static final Map<?, ?> addressCache_Map = _getCacheMap(addressCache);
    private static final Logger log = Log.getLogger();
    private static final Object negativeCache = _getCache("negativeCache");
    private static final Map<?, ?> negativeCache_Map = _getCacheMap(negativeCache);

    private static Object _getCache(String name) {
        try {
            Class<InetAddress> clazz = InetAddress.class;
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(clazz);
        } catch (Throwable e) {
            log.error("", e);
        }
        return null;
    }

    private static Map<?, ?> _getCacheMap(Object cache) {
        try {
            Class<?> c = cache.getClass();
            Field cacheMapField = c.getDeclaredField("cache");
            cacheMapField.setAccessible(true);
            Map<?, ?> cacheMap = (Map<?, ?>) cacheMapField.get(cache);
            return cacheMap;
        } catch (Throwable e) {
            log.error("", e);
        }
        return null;
    }

    public static void addressCacheClear() {
        synchronized (addressCache) {
            addressCache_Map.clear();
        }
    }

    public static Map<String, CacheDetail> addressCacheDetail() {
        return getCacheDetail(addressCache, addressCache_Map);
    }

    public static void cacheClear() {
        addressCacheClear();
        negativeCacheClear();
    }

    private static Map<String, CacheDetail> getCacheDetail(Object cache, Map<?, ?> cacheMap) {
        try {
            synchronized (cache) {
                Map<String, CacheDetail> map = new LinkedHashMap<String, CacheDetail>(cacheMap.size());
                for (Map.Entry<?, ?> e : cacheMap.entrySet()) {
                    Object cacheEntry = e.getValue();

                    Class<?> cacheEntryKlass = cacheEntry.getClass();
                    Field expf = cacheEntryKlass.getDeclaredField("expiration");
                    expf.setAccessible(true);
                    long expires = (Long) expf.get(cacheEntry);

                    Field af = cacheEntryKlass.getDeclaredField("address");
                    af.setAccessible(true);
                    InetAddress[] addresses = (InetAddress[]) af.get(cacheEntry);
                    List<String> ads = new ArrayList<String>(addresses.length);
                    for (InetAddress address : addresses) {
                        ads.add(address.getHostAddress());
                    }

                    map.put((String) e.getKey(), new CacheDetail(expires, addresses));
                }
                return map;
            }
        } catch (Throwable e) {
            log.error("", e);
        }
        return Collections.emptyMap();
    }

    public static StringBuilder printCache(StringBuilder sb) {
        sb.append("[addressCache]\n");
        String fmt = "%50s %19s %20s %s  \n";
        for (Map.Entry<String, CacheDetail> e : addressCacheDetail().entrySet()) {
            CacheDetail d = e.getValue();
            sb.append(String.format(fmt, e.getKey(), d.getExpire(), d.getRemain(), Arrays.toString(d.getAddress())));
        }
        sb.append("\n");
        sb.append("[negativeCache]\n");
        for (Map.Entry<String, CacheDetail> e : negativeCacheDetail().entrySet()) {
            CacheDetail d = e.getValue();
            sb.append(String.format(fmt, e.getKey(), d.getExpire(), d.getRemain(), Arrays.toString(d.getAddress())));
        }
        return sb;
    }

    public static boolean isUnknownHost(String host) {
        try {
            InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            // log.error("", e);
            return true;
        }
        return false;
    }

    public static boolean isUnknownHostByUrl(String url) {
        try {
            InetAddress.getByName(URI.create(url).getHost());
        } catch (UnknownHostException e) {
            // log.error("", e);
            return true;
        } catch (IllegalArgumentException e) {
            log.error("", e);
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        // TODO:怎么感觉没有生效，不过这种用法一般不用，直接 CacheClear就行了
        // InetAddressCachePolicy
        InetAddressCachePolicy.setIfNotSet(-1);
        InetAddressCachePolicy.setNegativeIfNotSet(40);

        System.out.println(InetAddressCachePolicy.get());
        System.out.println(InetAddressCachePolicy.getNegative());

        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");

        InetAddress.getByName("stackoverflow.com");
        InetAddress.getByName("www.google.com");
        InetAddress.getByName("www.yahoo.com");
        InetAddress.getByName("www.example.com");
        try {
            InetAddress.getByName("nowhere.example.com");
        } catch (UnknownHostException e) {
            System.out.println(e);

        }
        System.out.println(printCache(new StringBuilder()));
        cacheClear();

        InetAddress.getByName("stackoverflow.com");
        System.out.println(printCache(new StringBuilder()));
    }

    public static void negativeCacheClear() {
        synchronized (negativeCache) {
            negativeCache_Map.clear();
        }
    }

    public static Map<String, CacheDetail> negativeCacheDetail() {
        return getCacheDetail(negativeCache, negativeCache_Map);
    }
}
