package com.xunlei.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Map;
import com.xunlei.util.codec.Hex;

/**
 * <pre>
 * ���������˴�ӡһЩ�ַ����Ŀ�ݷ���
 * ���з��ػ���ֵΪ StringBuilder,�����ڲ��л��з��ŵ�
 * </pre>
 * 
 * @author ZengDong
 * @since 2010-9-19 ����07:25:38
 */
public class StringHelper {

    /**
     * ���Դ��key-valueֵ��StringBuilder
     * 
     * @author ZengDong
     */
    public interface KeyValueStringBuilder {

        public void append(StringBuilder tmp, Object key, Object value);
    }

    private static Method getOurStackTrace;

    static {
        try {
            getOurStackTrace = Throwable.class.getDeclaredMethod("getOurStackTrace");
            getOurStackTrace.setAccessible(true);
        } catch (Exception e) {
        }
    }

    /**
     * �����ɸ��������StringBuilder��
     * 
     * @param tmp StringBuilder���������
     * @param args �����б�
     * @return
     */
    public static StringBuilder append(StringBuilder tmp, Object... args) {
        for (Object s : args) {
            tmp.append(s);
        }
        return tmp;
    }

    /**
     * ���Ӷ���ַ���
     * 
     * @param args
     * @return
     */
    public static String concate(Object... args) {
        if (args.length < 4) {
            String result = "";
            for (Object s : args) {
                result += s;
            }
            return result;
        }
        return append(new StringBuilder(), args).toString();
    }

    public static <K, V> String concateKeyValue(KeyValueStringBuilder keyValueStringBuilder, Map<K, V> keyvalue) {
        return concateKeyValue(new StringBuilder(), keyValueStringBuilder, keyvalue).toString();
    }

    /**
     * ��
     * 
     * @param keyValueStringBuilder
     * @param keyvalue
     * @return
     */
    public static String concateKeyValue(KeyValueStringBuilder keyValueStringBuilder, Object... keyvalue) {
        return concateKeyValue(new StringBuilder(), keyValueStringBuilder, keyvalue).toString();
    }

    /**
     * ��map�д�ŵ����ɸ�key-value�������StringBuilder
     * 
     * @param <K> key����
     * @param <V> value����
     * @param tmp
     * @param keyValueStringBuilder
     * @param keyvalue
     * @return
     */
    public static <K, V> StringBuilder concateKeyValue(StringBuilder tmp, KeyValueStringBuilder keyValueStringBuilder, Map<K, V> keyvalue) {
        for (Map.Entry<K, V> entry : keyvalue.entrySet()) {
            keyValueStringBuilder.append(tmp, entry.getKey(), entry.getValue());
        }
        return tmp;
    }

    /**
     * �����key-value�Դ����StringBuilder��
     * 
     * @param tmp StringBuilder������
     * @param keyValueStringBuilder keyValueStringBuilder�ӿڵ�ʵ����Ķ���
     * @param keyvalue key-value�б�
     * @return
     */
    public static StringBuilder concateKeyValue(StringBuilder tmp, KeyValueStringBuilder keyValueStringBuilder, Object... keyvalue) {
        MapUtil.checkKeyValueLength(keyvalue);
        for (int i = 0; i < keyvalue.length; i++) {
            keyValueStringBuilder.append(tmp, keyvalue[i++], keyvalue[i]);
        }
        return tmp;
    }

    /**
     * ���Ӷ���ַ���,args����splitStr����
     * 
     * @param splitStr
     * @param args
     * @return
     */
    public static String concateWithSplit(String splitStr, Object... args) {
        return concateWithSplit(new StringBuilder(), splitStr, args).toString();
    }

    /**
     * ���Ӷ���ַ���,args����splitStr����,����StringBuilder
     * 
     * @param tmp StringBuilder������
     * @param splitStr ���ӷ�
     * @param args ����ַ���
     * @return
     */
    public static StringBuilder concateWithSplit(StringBuilder tmp, String splitStr, Object... args) {
        if (args.length == 0) {
            return tmp;
        }
        int endIndex = args.length - 1;
        for (int i = 0; i < endIndex; i++) {
            tmp.append(args[i]).append(splitStr);
        }
        tmp.append(args[endIndex]);
        return tmp;
    }

    /**
     * ��ӡ�����ַ���,�ò�ͬ�ָ����� �ѱ���Χ����,��ǿ������
     * 
     * @param title
     * @param corner
     * @param linechar
     * @param verticalchar
     * @return
     */
    public static StringBuilder emphasizeTitle(String title, char corner, char linechar, char verticalchar) {
        return emphasizeTitle(new StringBuilder(), title, corner, linechar, verticalchar);
    }

    /**
     * ��ӡ�����ַ���,�ò�ͬ�ָ����� �ѱ���Χ����,��ǿ������
     * 
     * @param tmp
     * @param title
     * @param corner
     * @param linechar
     * @param verticalchar
     * @return
     */
    public static StringBuilder emphasizeTitle(StringBuilder tmp, String title, char corner, char linechar, char verticalchar) {
        StringBuilder line;
        try {
            line = printLine(title.getBytes("GBK").length, corner, linechar);
            tmp.append(line);
            tmp.append(verticalchar).append(title).append(verticalchar).append('\n');
            tmp.append(line);
        } catch (UnsupportedEncodingException e) {
        }
        return tmp;
    }

    /**
     * ��ö�ջ������Ԫ��
     * 
     * @param ex
     * @return
     */
    private static StackTraceElement[] getOurStackTrace(Throwable ex) {
        try {
            StackTraceElement[] ste = (StackTraceElement[]) getOurStackTrace.invoke(ex);
            return ste;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ��ӡһ�� ����Ϊ len ���ظ�lineChar�ַ��ķָ���
     * 
     * @param len
     * @param linechar
     * @return
     */
    public static StringBuilder printLine(int len, char linechar) {
        return printLine(new StringBuilder(), len, linechar);
    }

    /**
     * ��ӡһ�� ����Ϊ len ���ظ�lineChar�ַ��ķָ���
     * 
     * @param len
     * @param corner ת�Ǵ���ʹ���ַ�
     * @param linechar Ĭ���ַ�
     * @return
     */
    public static StringBuilder printLine(int len, char corner, char linechar) {
        return printLine(new StringBuilder(), len, corner, linechar);
    }

    /**
     * ���������len��lineChar��ɵ��ַ���
     * 
     * @param tmp ��Ž����StringBuilder���������
     * @param len �ַ��ĸ���
     * @param linechar �ַ�
     * @return
     */
    public static StringBuilder printLine(StringBuilder tmp, int len, char linechar) {
        for (int i = 0; i < len; i++) {
            tmp.append(linechar);
        }
        tmp.append('\n');
        return tmp;
    }

    /**
     * ������corner��������len��lineChar�ַ���ɵ��ַ���
     * 
     * @param tmp ��Ž��������
     * @param len �ַ��ĸ���
     * @param corner ��������
     * @param linechar �ַ�
     * @return
     */
    public static StringBuilder printLine(StringBuilder tmp, int len, char corner, char linechar) {
        tmp.append(corner);
        for (int i = 0; i < len; i++) {
            tmp.append(linechar);
        }
        tmp.append(corner);
        tmp.append('\n');
        return tmp;
    }

    /**
     * ��ӡ ��ջ�쳣
     * 
     * @param tmp
     * @param ex
     * @return
     */
    public static StringBuilder printThrowable(StringBuilder tmp, Throwable ex) {
        if (null != ex) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter pw = new PrintWriter(stringWriter);
            ex.printStackTrace(pw);
            tmp.append(stringWriter).append('\n');
        }
        return tmp;
    }

    /**
     * ��ӡ ��ջ�쳣
     */
    public static StringBuilder printThrowable(Throwable ex) {
        return printThrowable(new StringBuilder(), ex);
    }

    /**
     * �򵥴�ӡThrowable��Ϣ�����8��
     * 
     * @param ex
     * @return
     */
    public static String printThrowableSimple(Throwable ex) {
        return printThrowableSimple(ex, 8);
    }

    /**
     * �����ӡһ��throwable��Ϣ,�����У���maxTraceLenָ����Ϣ����
     * 
     * @param ex
     * @param maxTraceLen Ҫ��ӡ��ջ��Ϣ������
     * @return
     */
    public static String printThrowableSimple(Throwable ex, int maxTraceLen) {
        if (null != ex) {
            StringBuilder s = new StringBuilder();
            s.append(ex.getClass().getSimpleName());// ���ﲻ��ӡȫ��
            s.append(":");
            s.append(ex.getMessage());
            if (maxTraceLen > 0) {
                // TODO:���ﲢû�д�ӡCauseThrowable��ص���Ϣ
                StackTraceElement[] trace = getOurStackTrace(ex);
                if (trace != null) {
                    int len = Math.min(trace.length, maxTraceLen);
                    for (int i = 0; i < len; i++) {
                        try {
                            StackTraceElement t = trace[i];
                            String clazzName = t.getClassName();
                            clazzName = clazzName.substring(clazzName.lastIndexOf(".") + 1, clazzName.length());
                            s.append("||");
                            s.append(clazzName);
                            s.append(".");
                            s.append(t.getMethodName());
                            s.append(":");
                            s.append(t.getLineNumber());
                        } catch (Exception e) {
                        }
                    }
                }
            }
            return s.toString();
        }
        return "";
    }

    /**
     * ��16���� ��ӡ�ֽ�����
     * 
     * @param bytes
     * @return
     */
    public static String printHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(bytes.length);
        int startIndex = 0;
        int column = 0;
        for (int i = 0; i < bytes.length; i++) {
            column = i % 16;
            switch (column) {
            case 0:
                startIndex = i;
                fixHexString(buffer, Integer.toHexString(i), 8).append(": ");
                buffer.append(toHex(bytes[i]));
                buffer.append(" ");
                break;
            case 15:
                buffer.append(toHex(bytes[i]));
                buffer.append(" ");
                buffer.append(filterString(bytes, startIndex, column + 1));
                buffer.append("\n");
                break;
            default:
                buffer.append(toHex(bytes[i]));
                buffer.append(" ");
            }
        }
        if (column != 15) {
            for (int i = 0; i < (15 - column); i++) {
                buffer.append("   ");
            }
            buffer.append(filterString(bytes, startIndex, column + 1));
            buffer.append("\n");
        }

        return buffer.toString();
    }

    /**
     * ��hexStr��ʽ����length����16�����������ں�߼���h
     * 
     * @param hexStr String
     * @return StringBuilder
     */
    private static StringBuilder fixHexString(StringBuilder buf, String hexStr, int length) {
        if (hexStr == null || hexStr.length() == 0) {
            buf.append("00000000h");
        } else {
            int strLen = hexStr.length();
            for (int i = 0; i < length - strLen; i++) {
                buf.append("0");
            }
            buf.append(hexStr).append("h");
        }
        return buf;
    }

    /**
     * ���ֽ�ת����16������ʾ
     * 
     * @param b byte
     * @return String
     */
    private static String toHex(byte b) {
        char[] buf = new char[2];
        byte bt = b;
        for (int i = 0; i < 2; i++) {
            buf[1 - i] = Hex.DIGITS_LOWER[bt & 0xF];
            bt = (byte) (bt >>> 4);
        }
        return new String(buf);
    }

    /**
     * ���˵��ֽ�������0x0 - 0x1F�Ŀ����ַ��������ַ���
     * 
     * @param bytes byte[]
     * @param offset int
     * @param count int
     * @return String
     */
    private static String filterString(byte[] bytes, int offset, int count) {
        byte[] buffer = new byte[count];
        System.arraycopy(bytes, offset, buffer, 0, count);
        for (int i = 0; i < count; i++) {
            if (buffer[i] >= 0x0 && buffer[i] <= 0x1F) {
                buffer[i] = 0x2e;
            }
        }
        return new String(buffer);
    }

    public static String digestString(String src) {
        return digestString(src, 50);
    }

    public static String digestString(String src, int lengthThreshold) {
        if (src.length() > lengthThreshold * 2 + 20) {
            return src.substring(0, lengthThreshold) + "...(" + src.length() + ")..." + src.substring(src.length() - lengthThreshold, src.length());
        }
        return src;
    }

    private StringHelper() {
    }
}
