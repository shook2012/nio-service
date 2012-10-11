package com.xunlei.util;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;

/**
 * @author ZengDong
 * @since 2010-11-10 ����08:21:47
 */
public class CloseableHelper {

    private static final Map<Class<?>, Method> closeMethodCache = new HashMap<Class<?>, Method>(2);
    private static final Logger log = Log.getLogger();

    /**
     * �رն��󣬹ر�ǰ���ж��Ƿ�Ϊnull�������׳��쳣
     * 
     * @param c
     * @throws IOException
     */
    public static void close(Closeable c) throws IOException {
        if (c != null) {
            c.close();
        }
    }

    /**
     * �رն��󣬹ر�ǰ���ж��Ƿ�Ϊnull�������׳��쳣
     * 
     * @param c
     * @throws Exception
     */
    public static void close(Object c) throws Exception {
        if (c != null) {
            Class<?> clazz = c.getClass();
            Method cachedMehtod = closeMethodCache.get(clazz);
            if (cachedMehtod == null) {
                Method m = clazz.getMethod("close");// close���������� public��,�������ʹ��getMethod,����getDeclaredMethod
                m.invoke(c);
                closeMethodCache.put(clazz, m);
            } else {
                cachedMehtod.invoke(c);
            }
        }
    }

    /**
     * �رն��󣬹ر�ǰ���ж��Ƿ�Ϊnull�������׳��쳣
     * 
     * <pre>
     * ע�⣺
     * Closing this socket will also close the socket's {@link java.io.InputStream InputStream} and {@link java.io.OutputStream OutputStream}.
     * If this socket has an associated channel then the channel is closed as well.
     * @param c
     * @throws IOException
     */
    public static void close(Socket c) throws IOException {
        if (c != null) {
            c.close();
        }
    }

    /**
     * �رն��󣬷�������ʹ����catch�������׳��쳣
     * 
     * @param c
     */
    public static void closeSilently(Closeable c) {
        try {
            close(c);
        } catch (Throwable e) {
            log.info("", e);
        }
    }

    /**
     * �رն��󣬷�������ʹ����catch�������׳��쳣
     * 
     * @param c
     */
    public static void closeSilently(Object c) {
        try {
            close(c);
        } catch (Throwable e) {
            log.info("", e);
        }
    }

    /**
     * �رն��󣬷�������ʹ����catch�������׳��쳣
     * 
     * @param c
     */
    public static void closeSilently(Socket c) {
        try {
            close(c);
        } catch (Throwable e) {
            log.info("", e);
        }
    }

    public static void closeAndLog(Closeable c) {
        try {
            log.info("close Closeable:{}", c);
            close(c);
        } catch (Throwable e) {
            log.info("", e);
        }
    }

    public static void closeAndLog(Object c) {
        try {
            log.info("close Object:{}", c);
            close(c);
        } catch (Throwable e) {
            log.info("", e);
        }
    }

    public static void closeAndLog(Socket c) {
        try {
            log.info("close Socket:{}", c);
            close(c);
        } catch (Throwable e) {
            log.info("", e);
        }
    }

    private CloseableHelper() {
    }
}
