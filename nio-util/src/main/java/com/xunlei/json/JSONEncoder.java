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
 * ������stringtree������ĳ��̰߳�ȫ�ķ�ʽ�� ���ṩ�򵥵ľ�̬���뷽��{@see JSONEncoder#encode(Object)}
 * 
 * @author stringtree.org
 * @author jindw
 * @author zengdong
 */
public class JSONEncoder {

    /**
     * ��־��¼��
     */
    private static final Logger log = Log.getLogger();

    /**
     * Ϊʲô��Ҫ���ʵ������
     */
    private static final JSONEncoder encoder = new JSONEncoder();
    /**
     * 16���Ƶ��ַ�����
     */
    private static final char[] hex = "0123456789ABCDEF".toCharArray();

    private JSONEncoder() {
    }

    /**
     * ������ת����JSON��ʽ
     * 
     * @param value ��ת���Ķ���
     * @return ת�����JSON
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
     * ������ת����JSON��ʽ��ֱ�ӵ���print
     * 
     * @param value ��ת���Ķ���
     * @param out ���ת�����������
     * @throws IOException
     */
    public void encode(Object value, StringBuilder out) throws IOException {
        print(value, out);
    }

    /**
     * ����ת����JSON��ʽ��ʵ�ʷ���
     * 
     * @param object ��ת���Ķ���
     * @param out ���ת�����������
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
            printDirectly(((Class<?>) object).getName(), out);// Class ϵ�л����׵�����ѭ��
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
     * ��cת��Ϊ16���ƣ����������out��
     * 
     * @param out ��Ž����StringBuilder
     * @param c ��ת�����ַ�
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
     * ֱ��ת���������ڴ��ַ����ַ���
     * 
     * @param text ��ת�����ַ���
     * @param out ���ת���Ľ��
     * @throws IOException
     */
    protected void printDirectly(String text, StringBuilder out) throws IOException {
        out.append('"');
        out.append(text);
        out.append('"');
    }

    /**
     * ���ں��пո񡢻س����Ʊ�����ַ�������ת��
     * 
     * @param text ��ת�����ַ���
     * @param out ���ת�������Ӧ��
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
     * ��JavaBeanת����JSON
     * 
     * @param object ��ת����JavaBean
     * @param out ���ת�����������
     * @throws IOException
     */
    protected void printBean(Object object, StringBuilder out) throws IOException {
        out.append('{');
        BeanInfo info;
        boolean addedSomething = false;
        try {
            // �Ƿ��б�Ҫ�Լ�ʵ����ʡ����
            info = Introspector.getBeanInfo(object.getClass());// Ϊ��ʹ����ʡ�е� ����,����ָ��filanClass
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
     * ��map����ת����JSON
     * 
     * @param map ��ת����Map����
     * @param out ת�����Ž��������
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
     * ����������ת����JSON
     * 
     * @param object ��ת����Object����
     * @param out ת�����Ž��������
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
     * �������еĶ���ת����JSON
     * 
     * @param collecion ��ת��������
     * @param out ת�����Ž��������
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
     * ���������ƶ������ɸ�����ת����JSON
     * 
     * @param it ��ת����Iterator����
     * @param out ת�����Ž��������
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
     * ��ö�ٶ���ת����JSON
     * 
     * @param it ��ת����ö�ٶ���
     * @param out ת�����Ž��������
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
