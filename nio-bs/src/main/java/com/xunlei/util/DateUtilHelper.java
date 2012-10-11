package com.xunlei.util;

/**
 * <pre>
 *  ͨ���������ù���:DateUtil.oper()�İ�����
 *  �������� ���������� MagicNum
 *  
 * @author ZengDong
 * @since 2010-10-21 ����10:14:35
 */
public class DateUtilHelper {

    public static final int SET = 5;
    public static final int ADD = 4;
    public static final int ROLL = 3;
    public static final int CEIL = 2;
    public static final int ROUND = 1;
    public static final int TRUNCATE = 0;

    /**
     * ����set calendarField��amount��MagicNum
     * 
     * @param calendarField
     * @param amount
     * @return
     */
    public static long set(int calendarField, int amount) {
        if (amount < 0) {
            return amount * 1000l - SET * 100 - calendarField;
        }
        return amount * 1000l + SET * 100 + calendarField;
    }

    /**
     * ���� ��ӦcalendarField add(amount)��MagicNum
     * 
     * @param calendarField
     * @param amount
     * @return
     */
    public static long add(int calendarField, int amount) {
        if (amount < 0) {
            return amount * 1000l - ADD * 100 - calendarField;
        }
        return amount * 1000l + ADD * 100 + calendarField;
    }

    /**
     * ���� ��ӦcalendarField roll(amount)��MagicNum
     * 
     * @param calendarField
     * @param amount
     * @return
     */
    public static long roll(int calendarField, int amount) {
        if (amount < 0) {
            return amount * 1000l - ROLL * 100 - calendarField;
        }
        return amount * 1000l + ROLL * 100 + calendarField;
    }

    /**
     * ���� ceil calendarField��MagicNum
     * 
     * @param calendarField
     * @return
     */
    public static long ceil(int calendarField) {
        return CEIL * 100 + calendarField;
    }

    /**
     * ���� round calendarField��MagicNum
     * 
     * @param calendarField
     * @return
     */
    public static long round(int calendarField) {
        return ROUND * 100 + calendarField;
    }

    /**
     * ���� truncate calendarField��MagicNum
     * 
     * @param calendarField
     * @return
     */
    public static long truncate(int calendarField) {
        return TRUNCATE * 100 + calendarField;
    }

    /**
     * ��MagicNum��ȡ��amount�ֶ�
     * 
     * @param operMagic
     * @return
     */
    public static int getAmount(long operMagic) {
        return (int) (operMagic / 1000);
    }

    /**
     * ��MagicNum��ȡ�����������ֶ�
     * 
     * @param operMagic
     * @return
     */
    public static int getOperType(long operMagic) {
        int r = (int) (operMagic % 1000 / 100);
        return Math.abs(r);
    }

    /**
     * ��MagicNum��ȡ��CalendarField�ֶ�
     * 
     * @param operMagic
     * @return
     */
    public static int getCalendarField(long operMagic) {
        int r = (int) (operMagic % 100);
        return Math.abs(r);
    }
}
