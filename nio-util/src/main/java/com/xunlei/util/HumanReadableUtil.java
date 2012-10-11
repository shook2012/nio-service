package com.xunlei.util;

import java.text.DecimalFormat;

/**
 * �ṩ��ʱ�����Ӳ�̿ռ��Сת�������Ƿ����Ķ���ʽ�ķ���
 * 
 * @author ZengDong
 * @since 2010-7-9 ����11:24:06
 */
public class HumanReadableUtil {

    /**
     * Ӳ�����̵ı�׼��ÿ���ֽڵ�λ���е��ֽ���
     */
    private static final double[] BYTE_SIZE = { 1, 1024, 1024 * 1024, 1024 * 1024 * 1024, 1024d * 1024 * 1024 * 1024 };
    /**
     * �ֽڵ�λ��ʾ
     */
    private static final String[] BYTE_SIZE_FORMAT = { "B", "KB", "MB", "GB", "TB" };
    /**
     * ����ϵͳ��ÿ����λ���е��ֽ���
     */
    private static final double[] BYTE_SIZE_HD = { 1, 1000, 1000 * 1000, 1000 * 1000 * 1000, 1000d * 1000 * 1000 * 1000 };
    /**
     * 
     */
    private static final double[] BYTE_SIZE_THRESHOLD = { 1, 999, 999 * 1024, 999 * 1024 * 1024, 999d * 1024 * 1024 * 1024 };
    /**
     * 
     */
    private static final String NA = "N/A";
    /**
     * ÿ��ʱ�䵥λ��Ӧ�ĺ�����
     */
    private static final long[] TIME_SPAN = { 1, 1000, 1000 * 60, 1000 * 60 * 60, 1000 * 60 * 60 * 24, 1000l * 60 * 60 * 24 * 365 };
    /**
     * ʱ�䵥λ(����)
     */
    private static final String[] TIME_SPAN_FORMAT_CH = { "����", "��", "��", "ʱ", "��", "��" };
    /**
     * ʱ�䵥λ(Ӣ��)
     */
    private static final String[] TIME_SPAN_FORMAT_EN = { "ms ", "sec ", "min ", "hour ", "day ", "year " };

    /**
     * ��Ӳ�����̵ı�׼���ļ���Сת���ɱ�׼��ʽ
     * 
     * @param length �ļ��ֽڳ���
     * @return
     */
    public static String byteSize(long size) {
        return byteSize(size, false);
    }

    /**
     * ��GB,MB,KB,B��̬��ʾ�ļ�(��Ӳ��)��С ������λ��
     * 
     * @param bytes �ֽڴ�С
     * @param byHDStandard Ӳ�̳��̵ı�׼��40GB=40,000MB=40,000,000KB=40,000,000,000byte�� ����ϵͳ���㷨��40GB=40,960MB=41,943,040KB=42,949,672,960byte�� Ϊtrueʱ����������Ӳ�̳��̵ı�׼��ʾ
     * @return String �ļ�(��Ӳ��)��С��ʾ
     */
    public static String byteSize(long bytes, boolean byHDStandard) {
        if (bytes < 0) {
            return NA;
        }
        double[] byte_size = BYTE_SIZE;
        double[] byte_size_threadshold = BYTE_SIZE_THRESHOLD;
        if (byHDStandard) {
            byte_size = BYTE_SIZE_HD;
            byte_size_threadshold = BYTE_SIZE_HD;
        }
        int i = 5;
        DecimalFormat df = new DecimalFormat("##.##");
        while (--i >= 0) {
            if (bytes >= byte_size_threadshold[i]) {
                return df.format((bytes / byte_size[i])) + BYTE_SIZE_FORMAT[i];
            }
        }
        return "";
    }

    /**
     * ���ú����ʾ��ʱ��ת�����������ձ�ʾ��ʱ��
     * 
     * @param span ʱ��ĺ�������ʾ
     * @return ʱ��������յĸ�ʽ
     */
    public static String timeSpan(long span) {
        return timeSpan(span, 0, false);
    }

    /**
     * �������ʽ��ʱ��ת��Ϊ**��**��**��**ʱ**��**��**����ĸ�ʽ
     * 
     * @param span ʱ��ĺ�������ʾ
     * @param max_len �����ʾ��ʱ�䵥λ����
     * @param chinese �Ƿ�Ϊ����
     * @return
     */
    public static String timeSpan(long span, int max_len, boolean chinese) {
        long sp = span;
        int maxlen = max_len;
        if (sp < 0) {
            return NA;
        }
        String[] format = chinese ? TIME_SPAN_FORMAT_CH : TIME_SPAN_FORMAT_EN;
        if (maxlen <= 0) {
            maxlen = 3;
        }
        long tmp = 0;
        int index = 6;
        StringBuilder sb = new StringBuilder("");
        while (--index >= 0) {
            if ((tmp = sp / TIME_SPAN[index]) > 0) {
                sp = sp % TIME_SPAN[index];
                sb.append(tmp);
                sb.append(format[index]);
                if (--maxlen <= 0) {
                    break;
                }
            }
        }
        return sb.toString();
    }
}
