package com.xunlei.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Map;
import com.xunlei.util.codec.Hex;

/**
 * <pre>
 * 这里罗列了打印一些字符串的快捷方法
 * 所有返回基本值为 StringBuilder,且是内部有换行符号的
 * </pre>
 * 
 * @author ZengDong
 * @since 2010-9-19 下午07:25:38
 */
public class StringHelper {

    /**
     * 可以存放key-value值的StringBuilder
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
     * 将若干个对象存入StringBuilder中
     * 
     * @param tmp StringBuilder对象的引用
     * @param args 对象列表
     * @return
     */
    public static StringBuilder append(StringBuilder tmp, Object... args) {
        for (Object s : args) {
            tmp.append(s);
        }
        return tmp;
    }

    /**
     * 连接多个字符串
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
     * 将
     * 
     * @param keyValueStringBuilder
     * @param keyvalue
     * @return
     */
    public static String concateKeyValue(KeyValueStringBuilder keyValueStringBuilder, Object... keyvalue) {
        return concateKeyValue(new StringBuilder(), keyValueStringBuilder, keyvalue).toString();
    }

    /**
     * 将map中存放的若干个key-value对象存入StringBuilder
     * 
     * @param <K> key类型
     * @param <V> value类型
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
     * 将多个key-value对村放入StringBuilder中
     * 
     * @param tmp StringBuilder的引用
     * @param keyValueStringBuilder keyValueStringBuilder接口的实现类的对象
     * @param keyvalue key-value列表
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
     * 连接多个字符串,args间用splitStr连接
     * 
     * @param splitStr
     * @param args
     * @return
     */
    public static String concateWithSplit(String splitStr, Object... args) {
        return concateWithSplit(new StringBuilder(), splitStr, args).toString();
    }

    /**
     * 连接多个字符串,args间用splitStr连接,返回StringBuilder
     * 
     * @param tmp StringBuilder的引用
     * @param splitStr 连接符
     * @param args 多个字符串
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
     * 打印三行字符串,用不同分隔符号 把标题围起来,起强调作用
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
     * 打印三行字符串,用不同分隔符号 把标题围起来,起强调作用
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
     * 获得堆栈的所有元素
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
     * 打印一行 长度为 len 的重复lineChar字符的分隔符
     * 
     * @param len
     * @param linechar
     * @return
     */
    public static StringBuilder printLine(int len, char linechar) {
        return printLine(new StringBuilder(), len, linechar);
    }

    /**
     * 打印一行 长度为 len 的重复lineChar字符的分隔符
     * 
     * @param len
     * @param corner 转角处所使用字符
     * @param linechar 默认字符
     * @return
     */
    public static StringBuilder printLine(int len, char corner, char linechar) {
        return printLine(new StringBuilder(), len, corner, linechar);
    }

    /**
     * 获得有数量len个lineChar组成的字符串
     * 
     * @param tmp 存放结果的StringBuilder对象的引用
     * @param len 字符的个数
     * @param linechar 字符
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
     * 返回以corner结束的由len个lineChar字符组成的字符串
     * 
     * @param tmp 存放结果的引用
     * @param len 字符的个数
     * @param corner 结束符号
     * @param linechar 字符
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
     * 打印 堆栈异常
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
     * 打印 堆栈异常
     */
    public static StringBuilder printThrowable(Throwable ex) {
        return printThrowable(new StringBuilder(), ex);
    }

    /**
     * 简单打印Throwable信息，最多8个
     * 
     * @param ex
     * @return
     */
    public static String printThrowableSimple(Throwable ex) {
        return printThrowableSimple(ex, 8);
    }

    /**
     * 精简打印一个throwable信息,不换行，由maxTraceLen指定信息数量
     * 
     * @param ex
     * @param maxTraceLen 要打印堆栈信息的数量
     * @return
     */
    public static String printThrowableSimple(Throwable ex, int maxTraceLen) {
        if (null != ex) {
            StringBuilder s = new StringBuilder();
            s.append(ex.getClass().getSimpleName());// 这里不打印全称
            s.append(":");
            s.append(ex.getMessage());
            if (maxTraceLen > 0) {
                // TODO:这里并没有打印CauseThrowable相关的信息
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
     * 以16进制 打印字节数组
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
     * 将hexStr格式化成length长度16进制数，并在后边加上h
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
     * 将字节转换成16进制显示
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
     * 过滤掉字节数组中0x0 - 0x1F的控制字符，生成字符串
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
