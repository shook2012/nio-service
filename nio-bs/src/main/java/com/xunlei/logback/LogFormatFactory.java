package com.xunlei.logback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ��� ͳ����־��Ӧ�� ��ʽ���ַ���
 * 
 * @author ZengDong
 * @since 2010-10-19 ����09:56:32
 */
public class LogFormatFactory {

    /**
     * �����־��¼��������
     */
    private static final Map<String, LogFormatFactory> LFF_CACHE_MAP = new ConcurrentHashMap<String, LogFormatFactory>(1);

    /**
     * �����־��¼����ÿһ��split��Ӧһ����־��¼��
     * 
     * @param split
     * @return
     */
    public static LogFormatFactory getInstance(String split) {
        LogFormatFactory lff = LFF_CACHE_MAP.get(split);
        if (lff == null) {
            LogFormatFactory new_lff = new LogFormatFactory(split);
            LogFormatFactory new_lff1 = LFF_CACHE_MAP.put(split, new_lff);
            lff = new_lff1 == null ? new_lff : new_lff1;
        }
        return lff;
    }

    // public static void main(String[] args) {
    // LogFormatFactory lff = LogFormatFactory.getInstance("=>");
    // System.out.println(lff.getFormat(3));
    // System.out.println(lff.getFormat(400));
    // System.out.println(lff.getFormat(2));
    // }
    private String _maxArgsLenFormat = "";
    private List<String> formats;
    /**
     * ÿ����־��¼������һ��split������������������־��¼��
     */
    private String split;

    private LogFormatFactory(String split) {
        this.formats = new ArrayList<String>();
        this.split = split;
    }

    /**
     * ????????????????????????????????
     * 
     * @param argsLen
     * @return
     */
    public String getFormat(int argsLen) {
        int index = argsLen - 1;
        if (formats.size() >= argsLen) {
            return formats.get(index);
        }
        synchronized (this) {
            if (formats.size() < argsLen) {
                StringBuilder tmp = new StringBuilder(_maxArgsLenFormat);
                for (int i = formats.size(); i < argsLen; i++) {
                    formats.add(tmp.append("{}").toString());
                    tmp.append(split);
                }
                _maxArgsLenFormat = tmp.toString();
            }
            return formats.get(index);
        }
    }

    public String getFormat(Object[] args) {
        if (args == null) {
            return "";
        }
        return getFormat(args.length);
    }
}
