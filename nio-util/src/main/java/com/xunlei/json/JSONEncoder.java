package com.xunlei.json;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import com.xunlei.util.Log;

/**
 * 改造自stringtree，将类改成线程安全的方式。 并提供简单的静态编码方法{@see JSONEncoder#encode(Object)}
 * 
 * @author stringtree.org
 * @author jindw
 * @author zengdong
 */
public class JSONEncoder {

    /**
     * 日志记录器
     */
    private static final Logger log = Log.getLogger();

    /**
     * 为什么需要这个实例对象？
     */
    private static final JSONEncoder encoder = new JSONEncoder();
    /**
     * 16进制的字符数组
     */
    private static final char[] hex = "0123456789ABCDEF".toCharArray();

    private JSONEncoder() {
    }

    /**
     * 将对象转化成JSON格式
     * 
     * @param value 待转换的对象
     * @return 转换后的JSON
     */
    public static String encode(Object value) {
        StringBuilder buf = new StringBuilder();
        try {
            encoder.encode(value, buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buf.toString();
    }

    /**
     * 将对象转化成JSON格式，直接调用print
     * 
     * @param value 待转换的对象
     * @param out 存放转换结果的引用
     * @throws IOException
     */
    public void encode(Object value, StringBuilder out) throws IOException {
        print(value, out);
    }

    /**
     * 对象转化成JSON格式的实际方法
     * 
     * @param object 待转换的对象
     * @param out 存放转换结果的引用
     * @throws IOException
     */
    protected void print(Object object, StringBuilder out) throws IOException {
        if (object == null) {
            out.append("null");
        } else if (object instanceof Boolean) {
            out.append(String.valueOf(object));
        } else if (object instanceof Number) {
            out.append(String.valueOf(object));
        } else if (object instanceof Class<?>) {
            printDirectly(((Class<?>) object).getName(), out);// Class 系列化容易导致死循环
        } else if (object instanceof String) {
            print((String) object, out);
        } else if (object instanceof Character) {
            print(String.valueOf(object), out);
        } else {
            if (object instanceof Map<?, ?>) {
                print((Map<?, ?>) object, out);
            } else if (object instanceof Object[]) {
                print((Object[]) object, out);
            } else if (object instanceof Iterator<?>) {
                print((Iterator<?>) object, out);
            } else if (object instanceof Enumeration<?>) {
                print((Enumeration<?>) object, out);
            } else if (object instanceof Collection<?>) {
                print(((Collection<?>) object), out);
            } else {
                printBean(object, out);
            }
        }
    }

    /**
     * 将c转化为16进制，将结果放入out中
     * 
     * @param out 存放结果的StringBuilder
     * @param c 带转化的字符
     */
    private void unicode(StringBuilder out, char c) {
        out.append("\\u");
        int n = c;
        for (int i = 0; i < 4; ++i) {
            int digit = (n & 0xF000) >> 12;
            out.append(hex[digit]);
            n <<= 4;
        }
    }

    /**
     * 直接转换，适用于纯字符的字符串
     * 
     * @param text 待转换的字符串
     * @param out 存放转换的结果
     * @throws IOException
     */
    protected void printDirectly(String text, StringBuilder out) throws IOException {
        out.append('"');
        out.append(text);
        out.append('"');
    }

    /**
     * 对于含有空格、回车、制表符的字符串进行转换
     * 
     * @param text 待转换的字符串
     * @param out 存放转换结果的应用
     * @throws IOException
     */
    protected void print(String text, StringBuilder out) throws IOException {
        out.append('"');
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\\') {
                out.append('\\');
                out.append(c);
            } else if ((c >= 0x0000 && c <= 0x001F) || (c >= 0x007F && c <= 0x009F)) {
                switch (c) {
                case '\b':// \u0008
                    out.append("\\b");
                    break;
                case '\t'://
                    out.append("\\t");
                    break;
                case '\n'://
                    // case '\v'://\u000b
                    out.append("\\n");
                    break;
                case '\f'://
                    out.append("\\f");
                    break;
                case '\r'://
                    out.append("\\r");
                    break;
                default:
                    // out.append("\\u");
                    // out.append(Integer.toHexString(0x10000 + c), 1, 5);
                    unicode(out, c);
                }
            } else {
                out.append(c);
            }
        }
        out.append('"');
    }

    /**
     * 将JavaBean转化成JSON
     * 
     * @param object 待转换的JavaBean
     * @param out 存放转换结果的引用
     * @throws IOException
     */
    protected void printBean(Object object, StringBuilder out) throws IOException {
        out.append('{');
        BeanInfo info;
        boolean addedSomething = false;
        try {
            // 是否有必要自己实现内省功能
            info = Introspector.getBeanInfo(object.getClass());// 为了使用内省中的 缓存,不能指定filanClass
            PropertyDescriptor[] props = info.getPropertyDescriptors();
            for (int i = 0; i < props.length; ++i) {
                PropertyDescriptor prop = props[i];
                if (!Class.class.equals(prop.getPropertyType())) {
                    Method accessor = prop.getReadMethod();
                    if (accessor != null) {
                        if (!accessor.isAccessible()) {
                            accessor.setAccessible(true);
                        }
                        Object value = accessor.invoke(object);
                        if (addedSomething) {
                            out.append(',');
                        }
                        String name = prop.getName();
                        printDirectly(name, out);
                        out.append(':');
                        print(value, out);
                        addedSomething = true;
                    }
                }
            }
        } catch (IllegalAccessException iae) {
            log.error("", iae);
        } catch (InvocationTargetException ite) {
            log.error("", ite);
        } catch (IntrospectionException ie) {
            log.error("", ie);
        }
        out.append('}');
    }

    /**
     * 将map对象转化成JSON
     * 
     * @param map 待转换的Map对象
     * @param out 转换后存放结果的引用
     * @throws IOException
     */
    protected void print(Map<?, ?> map, StringBuilder out) throws IOException {
        out.append('{');
        boolean addedSomething = false;
        for (Map.Entry<?, ?> e : map.entrySet()) {
            if (addedSomething) {
                out.append(',');
            }
            addedSomething = true;
            print(String.valueOf(e.getKey()), out);
            out.append(':');
            print(e.getValue(), out);
        }
        out.append('}');
    }

    /**
     * 将对象数组转换成JSON
     * 
     * @param object 待转换的Object数组
     * @param out 转换后存放结果的引用
     * @throws IOException
     */
    protected void print(Object[] object, StringBuilder out) throws IOException {
        out.append('[');
        for (int i = 0; i < object.length; ++i) {
            if (i > 0) {
                out.append(',');
            }
            print(object[i], out);
        }
        out.append(']');
    }

    /**
     * 将容器中的对象转换成JSON
     * 
     * @param collecion 待转换的容器
     * @param out 转换后存放结果的引用
     * @throws IOException
     */
    protected void print(Collection<?> collecion, StringBuilder out) throws IOException {
        out.append('[');
        boolean addedSomething = false;
        for (Object obj : collecion) {
            if (addedSomething) {
                out.append(',');
            }
            addedSomething = true;
            print(obj, out);
        }
        out.append(']');
    }

    /**
     * 将迭代器制定的若干个对象转换成JSON
     * 
     * @param it 待转换的Iterator对象
     * @param out 转换后存放结果的引用
     * @throws IOException
     */
    protected void print(Iterator<?> it, StringBuilder out) throws IOException {
        out.append('[');
        while (it.hasNext()) {
            print(it.next(), out);
            if (it.hasNext()) {
                out.append(',');
            }
        }
        out.append(']');
    }

    /**
     * 将枚举对象转换成JSON
     * 
     * @param it 待转换的枚举对象
     * @param out 转换后存放结果的引用
     * @throws IOException
     */
    protected void print(Enumeration<?> it, StringBuilder out) throws IOException {
        out.append('[');
        while (it.hasMoreElements()) {
            print(it.nextElement(), out);
            if (it.hasMoreElements()) {
                out.append(',');
            }
        }
        out.append(']');
    }
}
