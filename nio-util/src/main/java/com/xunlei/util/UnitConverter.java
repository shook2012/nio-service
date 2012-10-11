package com.xunlei.util;

/**
 * ��λ������,ʹ�ô�����ٳ��ֲ�����������,���׶�
 * 
 * @author ZengDong
 * @since 2011-5-23 ����02:03:19
 */
public class UnitConverter {

    public enum RmbUnit {
        fen(1), jiao(10), yuan(100);

        private long value;

        private RmbUnit(long value) {
            this.value = value;
        }

        public long get() {
            return value;
        }
    }

    public enum TimeUnit {
        millisecond(1),
        second(DateUtils.MILLIS_PER_SECOND),
        minute(DateUtils.MILLIS_PER_MINUTE),
        hour(DateUtils.MILLIS_PER_HOUR),
        day(DateUtils.MILLIS_PER_DAY),
        week(DateUtils.MILLIS_PER_DAY * 7),
        month31(DateUtils.MILLIS_PER_DAY * 31),
        month30(DateUtils.MILLIS_PER_DAY * 30),
        year365(DateUtils.MILLIS_PER_DAY * 365),

        month(DateUtils.MILLIS_PER_DAY * 30),
        year(DateUtils.MILLIS_PER_DAY * 365);

        private long value;

        private TimeUnit(long value) {
            this.value = value;
        }

        public long get() {
            return value;
        }
    }

    public enum ByteUnit {
        b(1), kb(1024l), mb(1024l * 1024), gb(1024l * 1024 * 1024), tb(1024l * 1024 * 1024 * 1024);

        private long value;

        private ByteUnit(long value) {
            this.value = value;
        }

        public long get() {
            return value;
        }
    }

    /**
     * ���from��ֵ��to��ֵ�ı��������������Ƿ�����ȡ��
     * 
     * @param from from
     * @param to
     * @param ceil �Ƿ�����ȡ��
     * @return
     */
    private static double convert(long from, long to, boolean ceil) {
        if (ceil) {
            return ceil((double) from / to);
        }
        return (from / to);
    }

    /**
     * �������ȡ������ֵ
     * 
     * @param num
     * @return
     */
    private static double ceil(double num) {
        if (num < 0) {
            return -Math.ceil(-num);
        }
        return Math.ceil(num);
    }

    /**
     * ����λΪfromUnit���ֽ���fromValueת��ΪtoUnit��λ���ֽ���
     * 
     * @param fromValue ԭ��ֵ
     * @param fromUnit ԭ��λ
     * @param toUnit Ŀ�굥λ
     * @return Ŀ����ֵ
     */
    public static long convertByte(long fromValue, ByteUnit fromUnit, ByteUnit toUnit) {
        return (long) convertByte(fromValue, fromUnit, toUnit, false);
    }

    /**
     * ����λΪfromUnit���ֽ���fromValueת��ΪtoUnit��λ���ֽ�����������ȡ��
     * 
     * @param fromValue ԭ��ֵ
     * @param fromUnit ԭ��λ
     * @param toUnit Ŀ�굥λ
     * @return Ŀ����ֵ
     */
    public static long convertByteCeil(long fromValue, ByteUnit fromUnit, ByteUnit toUnit) {
        return (long) convertByte(fromValue, fromUnit, toUnit, true);
    }

    /**
     * ����λΪfromUnit���ֽ���fromValueת��ΪtoUnit��λ���ֽ��������������Ƿ�����ȡ��
     * 
     * @param fromValue ԭ��ֵ
     * @param fromUnit ԭ��λ
     * @param toUnit Ŀ�굥λ
     * @param ceil �Ƿ�����ȡ��
     * @return Ŀ����ֵ
     */
    public static double convertByte(long fromValue, ByteUnit fromUnit, ByteUnit toUnit, boolean ceil) {
        return convert(fromValue * fromUnit.get(), toUnit.get(), ceil);
    }

    /**
     * ����λΪfromUnit��ʱ��fromValueת��ΪtoUnit��λ��ʱ��
     * 
     * @param fromValue ԭ��ֵ
     * @param fromUnit ԭ��λ
     * @param toUnit Ŀ�굥λ
     * @return Ŀ����ֵ
     */
    public static long convertTime(long fromValue, TimeUnit fromUnit, TimeUnit toUnit) {
        return (long) convertTime(fromValue, fromUnit, toUnit, false);
    }

    /**
     * ����λΪfromUnit��ʱ��fromValueת��ΪtoUnit��λ��ʱ�䣬������ȡ��
     * 
     * @param fromValue ԭ��ֵ
     * @param fromUnit ԭ��λ
     * @param toUnit Ŀ�굥λ
     * @return Ŀ����ֵ
     */
    public static long convertTimeCeil(long fromValue, TimeUnit fromUnit, TimeUnit toUnit) {
        return (long) convertTime(fromValue, fromUnit, toUnit, true);
    }

    /**
     * ����λΪfromUnit��ʱ��fromValueת��ΪtoUnit��λ��ʱ�䣬���������Ƿ�����ȡ��
     * 
     * @param fromValue ԭ��ֵ
     * @param fromUnit ԭ��λ
     * @param toUnit Ŀ�굥λ
     * @param ceil �Ƿ�����ȡ��
     * @return Ŀ����ֵ
     */
    public static double convertTime(long fromValue, TimeUnit fromUnit, TimeUnit toUnit, boolean ceil) {
        return convert(fromValue * fromUnit.get(), toUnit.get(), ceil);
    }

    /**
     * ����λΪfromUnit�������fromValueת��ΪtoUnit��λ�������
     * 
     * @param fromValue ԭ��ֵ
     * @param fromUnit ԭ��λ
     * @param toUnit Ŀ�굥λ
     * @return Ŀ����ֵ
     */
    public static long convertRmb(long fromValue, RmbUnit fromUnit, RmbUnit toUnit) {
        return (long) convertRmb(fromValue, fromUnit, toUnit, false);
    }

    /**
     * ����λΪfromUnit�������fromValueת��ΪtoUnit��λ������ң�������ȡ��
     * 
     * @param fromValue ԭ��ֵ
     * @param fromUnit ԭ��λ
     * @param toUnit Ŀ�굥λ
     * @return Ŀ����ֵ
     */
    public static long convertTimeCeil(long fromValue, RmbUnit fromUnit, RmbUnit toUnit) {
        return (long) convertRmb(fromValue, fromUnit, toUnit, true);
    }

    /**
     * ����λΪfromUnit�������fromValueת��ΪtoUnit��λ������ң����������Ƿ�����ȡ��
     * 
     * @param fromValue ԭ��ֵ
     * @param fromUnit ԭ��λ
     * @param toUnit Ŀ�굥λ
     * @param ceil �Ƿ�����ȡ��
     * @return Ŀ����ֵ
     */
    public static double convertRmb(long fromValue, RmbUnit fromUnit, RmbUnit toUnit, boolean ceil) {
        return convert(fromValue * fromUnit.get(), toUnit.get(), ceil);
    }

    public static void main(String[] args) {
        System.out.println(convertTime(2, TimeUnit.day, TimeUnit.millisecond));
        System.out.println(convertTime(1, TimeUnit.year, TimeUnit.day));
        System.out.println(convertTimeCeil(1, TimeUnit.day, TimeUnit.year));
        System.out.println(convertByte(1, ByteUnit.kb, ByteUnit.b));
        System.out.println(Math.ceil(-3.2));
    }

    /**
     * Ĭ�Ϲ��췽��
     */
    private UnitConverter() {
    }
}
