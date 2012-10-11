package com.xunlei.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.xunlei.util.codec.Base64Util;

/**
 * �ַ���������
 * 
 * @author ZengDong
 */
public final class StringTools {

    /**
     * �ж��ַ����Ƿ�Ϊ�գ��ж�ʱ��β�ո�ᱻȥ����
     * 
     * @param str
     * @return
     */
    public static boolean isBlank(CharSequence str) {
        if (str == null) {
            return true;
        }
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c > ' ') {
                return false;
            }
        }
        return true;
    }

    /**
     * edit by zengdong:2010-11-10 Ϊ����apache-common-lang��StringUtil�Ķ���һ��,�޸ĳɲ��ж���β�ո����,һ�������ʹ��isEmpty�Ϳ�����,����isBlank
     * 
     * @param str
     * @return
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    /**
     * �ж��ַ����Ƿ�Ϊ�գ��ж�ʱ��β�ո�ᱻȥ����
     * 
     * @param str
     * @return
     */
    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    /**
     * �ж��ַ����Ƿ��ǿյ�
     * 
     * @param str
     * @return
     */
    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    /**
     * �ж��ַ����Ƿ��������ַ���
     * 
     * @param str
     * @return
     */
    public static boolean isNotNumberStr(CharSequence str) {
        return !isNumberStr(str);
    }

    /**
     * �ж��ַ����Ƿ�Ϊ�����ַ���
     * 
     * @param str
     * @return
     */
    public static boolean isNumberStr(CharSequence str) {
        if (isEmpty(str)) {
            return false;
        }
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * ɾ���ַ����еĻ��з���ɾ��\r,\n��
     * 
     * @param src
     * @return
     */
    public static String removeNewLines(String src) {
        if (isNotEmpty(src)) {
            return src.replace("\n", "").replace("\r", "");
        }
        return "";
    }

    /**
     * �ַ���split��,��trim,ȥ�����ַ���
     * 
     * @param str
     * @param regex
     * @return
     */
    public static List<String> splitAndTrim(String str, String regex) {
        String[] arr = str.split(regex);
        List<String> list = new ArrayList<String>(arr.length);
        for (String a : arr) {
            String add = a.trim();
            if (add.length() > 0) {
                list.add(add);
            }
        }
        return list;
    }

    /**
     * �ַ���split��,��trim,ȥ�����ַ���,������result�˼�����
     * 
     * @param str
     * @param regex
     * @param result
     * @return
     */
    public static Collection<String> splitAndTrim(String str, String regex, Collection<String> result) {
        String[] arr = str.split(regex);
        for (String a : arr) {
            String add = a.trim();
            if (add.length() > 0) {
                result.add(add);
            }
        }
        return result;
    }

    /**
     * �ַ���split��,��trim,ȥ�����ַ���,���շ���ת�ɶ�ӦcomponentClazz(ֻ֧�ֻ���)���͵ļ���
     * 
     * @param <T>
     * @param str
     * @param regex
     * @param result
     * @param componentClazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> splitAndTrim(String str, String regex, Collection<T> result, Class<T> componentClazz) {
        Method method = ValueUtil.getValueOfMethod(componentClazz);
        String[] arr = str.split(regex);
        for (String a : arr) {
            String add = a.trim();
            if (add.length() > 0) {
                try {
                    result.add((T) method.invoke(null, a));
                } catch (Exception e) {
                }
            }
        }
        return result;
    }

    /**
     * ���ַ����ָ�,trim���װ��String����
     * 
     * @param str
     * @param regex
     * @return
     */
    public static String[] splitAndTrimAsArray(String str, String regex) {
        return splitAndTrim(str, regex).toArray(ValueUtil.REF_ARRAY_STRING);
    }

    /**
     * <pre>
     * str
     *   --split()--> Collection<String>
     *   --toArray()--> T[]
     *   
     * �˷������Դ��� ԭʼ��������,�ʷ���ֵ�� Object,��õ�ֵ����ת��
     * </pre>
     * 
     * @param <T>
     * @param str
     * @param regex
     * @param componentClazz
     * @return
     */
    public static <T> Object splitAndTrimAsArray(String str, String regex, Class<T> componentClazz) {
        return ValueUtil.toArray(splitAndTrim(str, regex, new ArrayList<T>(2), componentClazz), componentClazz);
    }

    /**
     * �����ַ�����htmlת���ַ�
     */
    public static String escapeHtml(String source) {
        if (isNotEmpty(source)) {
            return source.replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace(" ", "&nbsp;").replace("\n", "<br/>").replace("\r", "");
        }
        return "";
    }

    /**
     * �����ַ�����xmlת���ַ�
     */
    public static String escapeXml(String source) {
        if (isNotEmpty(source)) {
            return source.replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("&", "&amp;").replace("'", "&apos;");
        }
        return "";
    }

    /**
     * �Դ�����ַ������нضϣ�������ȳ���length�ͽ�ȡǰlength���ַ����Ӵ������С��length��ԭ������
     */
    public static String truncate(String source, int length) {
        if (isEmpty(source) || length <= 0) {
            return source;
        }
        return source.length() > length ? source.substring(0, length) : source;
    }

    private static final String[] STRING_ESCAPE_LIST;
    static {
        STRING_ESCAPE_LIST = new String[93]; // ascii������Ҫת��ľ���\(93)
        STRING_ESCAPE_LIST['\\'] = "\\\\";
        STRING_ESCAPE_LIST['\"'] = "\\\"";
        STRING_ESCAPE_LIST['\''] = "\\\'";
        STRING_ESCAPE_LIST['\r'] = "\\r";
        STRING_ESCAPE_LIST['\n'] = "\\n";
        STRING_ESCAPE_LIST['\f'] = "\\f";
        STRING_ESCAPE_LIST['\t'] = "\\t";
        STRING_ESCAPE_LIST['\b'] = "\\b";
    }

    /**
     * ת��Ϊ""�п��õ��ַ���
     */
    public static String escapeString(String source) {
        if (isNotEmpty(source)) {
            StringBuilder sb = new StringBuilder(source.length() + 16);
            for (int i = 0; i < source.length(); i++) {
                char ch = source.charAt(i);
                if (ch < STRING_ESCAPE_LIST.length) {
                    String append = STRING_ESCAPE_LIST[ch];
                    sb.append(null != append ? append : ch);
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * <pre>
     * str
     *   --split()--> Collection<String>
     *   --toArray()--> T[]
     *   
     * �˷���ʹ������refArray�ķ�ʽ,���ܴ��� ԭʼ��������,������ֵ֧�ַ���
     * </pre>
     * 
     * @param <T>
     * @param str
     * @param regex
     * @param refArray
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes", "cast" })
    public static <T> T[] splitAndTrimAsArray(String str, String regex, T[] refArray) {
        return ValueUtil.toArray((Collection<T>) splitAndTrim(str, regex, new ArrayList(2), refArray.getClass().getComponentType()), refArray);
    }

    /**
     * ����Ѹ��ר����
     * 
     * @param oriUrl
     * @return
     */
    public static String encodeThunderUrl(String oriUrl) {
        return "thunder://" + Base64Util.encode("AA" + oriUrl + "ZZ");
    }

    /**
     * ����Ѹ��ר������,���url���Ϸ�����null
     * 
     * @param thunderUrl
     * @return
     */
    public static String decodeThunderUrl(String thunderUrl) {
        if (thunderUrl.startsWith("thunder://")) {
            String needDecodeStr = thunderUrl.substring(10);
            String decodedStr = Base64Util.decode(needDecodeStr);
            if (decodedStr.startsWith("AA") && decodedStr.endsWith("ZZ")) {
                return decodedStr.substring(2, decodedStr.length() - 2);
            }
        }
        return null;
    }

    public static StringBuilder clearStringBuilder(StringBuilder sb) {
        sb.delete(0, sb.length());
        return sb;
    }

    public static StringBuilder subStringBuilder(StringBuilder sb, int start, int end) {
        int count = sb.length();
        if (end < count) {
            sb.delete(end, count);
        }
        if (start > 0) {
            sb.delete(0, start);
        }
        return sb;
    }

    public static StringBuilder subStringBuilder(StringBuilder sb, int start) {
        if (start > 0) {
            sb.delete(0, start);
        }
        return sb;
    }

    private StringTools() {
    }

    public static String trim(String str) {// ����String.trim ���϶�ȫ�ǿո�Ĵ���
        int len = str.length();
        int count = str.length();
        int st = 0;

        while ((st < len) && ((str.charAt(st) <= ' ') || str.charAt(st) == '��')) {
            st++;
        }
        while ((st < len) && ((str.charAt(len - 1) <= ' ') || str.charAt(len - 1) == '��')) {
            len--;
        }
        return ((st > 0) || (len < count)) ? str.substring(st, len) : str;
    }

    public static void main(String[] args) {
        System.out.println("[" + trim("      ��asdfasdasf da�� fsdasfd    ") + "]");
        // String a =
        // "ftp://dygod3:dygod3@d090.dygod.org:2010/%E7%81%B5%E9%AD%82%E5%86%B2%E6%B5%AADVD%E4%B8%AD%E8%8B%B1%E5%8F%8C%E5%AD%97/[%E7%94%B5%E5%BD%B1%E5%A4%A9%E5%A0%82www.dy2018.net]%E7%81%B5%E9%AD%82%E5%86%B2%E6%B5%AADVD%E4%B8%AD%E8%8B%B1%E5%8F%8C%E5%AD%97.rmvb";
        // System.out.println(decodeThunderUrl(encodeThunderUrl(a)));
        //
        // System.out.println(Arrays.toString(splitAndTrimAsArray("1,2,3,4,5", ",", ValueUtil.REF_ARRAY_LONG)));
        // System.out.println(Arrays.toString((long[]) splitAndTrimAsArray("1,2,3,4,5", ",", long.class)));
        // System.out.println(ArraysUtil.deepToString(splitAndTrimAsArray("1,2,3,4,5", ",", long.class)));
        String str = "0123456789";
        StringBuilder sb = new StringBuilder(str);
        System.out.println(subStringBuilder(sb, 2));
        System.out.println(str.substring(2));
        System.out.println(subStringBuilder(sb, 2, 4));
        System.out.println(str.substring(2, 4));
    }
}
