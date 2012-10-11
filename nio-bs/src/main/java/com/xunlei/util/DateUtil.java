package com.xunlei.util;

import static com.xunlei.util.DateUtilHelper.ADD;
import static com.xunlei.util.DateUtilHelper.CEIL;
import static com.xunlei.util.DateUtilHelper.ROLL;
import static com.xunlei.util.DateUtilHelper.ROUND;
import static com.xunlei.util.DateUtilHelper.SET;
import static com.xunlei.util.DateUtilHelper.TRUNCATE;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import com.xunlei.util.UnitConverter.TimeUnit;

/**
 * ��������ص�һЩ����
 * 
 * <pre>
 * 1.����DateFormat��ʽ
 * 2.���̰߳�ȫ��DateFormat
 * 3.�̰߳�ȫ��DateFormat��ThreadLocal����
 * 4.�������ù��ܣ�set,add,roll;  ceil,round,truncate;
 * 5.ͨ���������ù���:oper �븨��ʹ��DateUtilHelper��ʹ��
 * 6.������ʱ���Ӧʱ����λ(TimeUnit)��ʱ��: getInterval
 * 
 * 
 * ���Ҫformat/parse����,��ʹ��DateStringUtil
 * 
 * @author ZengDong
 * @since 2010-6-7 ����06:36:41
 */
public class DateUtil {

    /*
     * >>>>>>����DateFormat��ʽ
     */
    public static final String DF_yyyyMMddHHmmss = "yyyyMMddHHmmss";
    public static final String DF_yyMMddHHmmss = "yyMMddHHmmss";
    public static final String DF_yyyyMMdd = "yyyyMMdd";
    public static final String DF_yyMMdd = "yyMMdd";
    public static final String DF_yyyy_MM_dd_HHmmss = "yyyy-MM-dd HH:mm:ss";
    public static final String DF_yy_MM_dd_HHmmss = "yy-MM-dd HH:mm:ss";
    public static final String DF_yyyy_MM_dd = "yyyy-MM-dd";
    public static final String DF_yy_MM_dd = "yy-MM-dd";
    public static final String DF_DEFAULT = DF_yyyy_MM_dd_HHmmss;
    public static final String DF_DEFAULT_DAY = DF_yyyy_MM_dd;
    public static final String DF_DEFAULT_GMT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    /*
     * >>>>>>���̰߳�ȫ��DateFormat,һ��ֻ���ڵ�ǰʱ���format/parse
     */
    public static final DateFormat UNSAFE_DF_yyyyMMddHHmmss = new SimpleDateFormat(DF_yyyyMMddHHmmss);
    public static final DateFormat UNSAFE_DF_yyMMddHHmmss = new SimpleDateFormat(DF_yyMMddHHmmss);
    public static final DateFormat UNSAFE_DF_yyyyMMdd = new SimpleDateFormat(DF_yyyyMMdd);
    public static final DateFormat UNSAFE_DF_yyMMdd = new SimpleDateFormat(DF_yyMMdd);
    public static final DateFormat UNSAFE_DF_yyyy_MM_dd_HHmmss = new SimpleDateFormat(DF_yyyy_MM_dd_HHmmss);
    public static final DateFormat UNSAFE_DF_yy_MM_dd_HHmmss = new SimpleDateFormat(DF_yy_MM_dd_HHmmss);
    public static final DateFormat UNSAFE_DF_yyyy_MM_dd = new SimpleDateFormat(DF_yyyy_MM_dd);
    public static final DateFormat UNSAFE_DF_yy_MM_dd = new SimpleDateFormat(DF_yy_MM_dd);
    public static final DateFormat UNSAFE_DF_DEFAULT = UNSAFE_DF_yyyy_MM_dd_HHmmss;
    public static final DateFormat UNSAFE_DF_DEFAULT_DAY = UNSAFE_DF_yyyy_MM_dd;
    /*
     * >>>>>>�̰߳�ȫ��DateFormat �� ThreadLocal��
     */
    public static final ThreadLocal<DateFormat> DEFAULT_DF_FACOTRY = makeDateFormatPerThread(DF_DEFAULT);
    public static final ThreadLocal<DateFormat> DEFAULT_DAY_DF_FACOTRY = makeDateFormatPerThread(DF_DEFAULT_DAY);
    public static final ThreadLocal<DateFormat> GMT_DF_FACOTRY = makeDateFormatPerThread(DF_DEFAULT_GMT, Locale.US, true, TimeZone.getTimeZone("GMT"));
    private static final int DEFAULT_COMPARE_YEAR = 1986;// Ϊ�˶Ա���Ȼ��/��Ȼ��,Ҫ������Ҫ�Աȵ�Year���ó�ͳһ,���ж��Ƿ�Ҫȡ��

    /**
     * ����̰߳�ȫDateFormat ������:ThreadLocal
     * 
     * @param pattern
     * @param locale
     * @param lenient
     * @param zone
     * @return
     */
    public static ThreadLocal<DateFormat> makeDateFormatPerThread(final String pattern, final Locale locale, final boolean lenient, final TimeZone zone) {
        return new ThreadLocal<DateFormat>() {

            @Override
            protected synchronized DateFormat initialValue() {
                try {
                    DateFormat df = locale == null ? new SimpleDateFormat(pattern) : new SimpleDateFormat(pattern, locale);
                    df.setLenient(lenient);
                    if (zone != null) {
                        df.setTimeZone(zone);
                    }
                    // df.setCalendar(null);
                    // df.setNumberFormat(null);
                    return df;
                } catch (Exception e) {
                    return null;
                }
            }
        };
    }

    /**
     * ����̰߳�ȫDateFormat ������:ThreadLocal
     */
    public static ThreadLocal<DateFormat> makeDateFormatPerThread(final String pattern) {
        return makeDateFormatPerThread(pattern, null, true, null);
    }

    /**
     * ��ĳʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩���趨Ϊһ���ض�ֵ
     * 
     * @param calendar Calendar���ͱ�ʾ��ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @param amount Ҫ���õ�ֵ
     * @return
     */
    public static Calendar set(Calendar calendar, int calendarField, int amount) {
        calendar.set(calendarField, amount);
        return calendar;
    }

    /**
     * ��ĳʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩���趨Ϊһ���ض�ֵ
     * 
     * @param date Date���͵�ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @param amount Ҫ���õ�ֵ
     * @return
     */
    public static Date set(Date date, int calendarField, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(calendarField, amount);
        return calendar.getTime();
    }

    /**
     * ��ĳʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩���趨Ϊһ���ض�ֵ
     * 
     * @param timeMillis �ú�������ʾ��ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @param amount
     * @return
     */
    public static long set(long timeMillis, int calendarField, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.set(calendarField, amount);
        return calendar.getTimeInMillis();
    }

    /**
     * ��ĳʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩������һ������
     * 
     * @param calendar Calendar������ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @param amount Ҫ���ӵ�����
     * @return Calendar���͵�ʱ��
     */
    public static Calendar add(Calendar calendar, int calendarField, int amount) {
        calendar.add(calendarField, amount);
        return calendar;
    }

    /**
     * ��ĳʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩������һ������
     * 
     * @param date Date���͵�ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @param amount Ҫ���ӵ���
     * @return Date���͵�ʱ��
     */
    public static Date add(Date date, int calendarField, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(calendarField, amount);
        return calendar.getTime();
    }

    /**
     * ��ĳʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩������һ������
     * 
     * @param timeMillis ��������ʾ��ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @param amount Ҫ���ӵ���
     * @return ��������ʾ��ʱ��
     */
    public static long add(long timeMillis, int calendarField, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.add(calendarField, amount);
        return calendar.getTimeInMillis();
    }

    /**
     * ��ĳʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩�Ϲ���һ������
     * 
     * @param calendar Calendar��ʾ��ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @param amount Ҫ����������
     * @return Calendar���͵Ĺ������ʱ��
     */
    public static Calendar roll(Calendar calendar, int calendarField, int amount) {
        calendar.roll(calendarField, amount);
        return calendar;
    }

    /**
     * ��ĳʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩�Ϲ���һ������
     * 
     * @param date Date���͵�ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @param amount Ҫ��������
     * @return Date���͵Ĺ������ʱ��
     */
    public static Date roll(Date date, int calendarField, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.roll(calendarField, amount);
        return calendar.getTime();
    }

    /**
     * ��ĳʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩�Ϲ���һ������
     * 
     * @param timeMillis ��������ʾ��ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @param amount Ҫ����������
     * @return ��������ʾ��ʱ��
     */
    public static long roll(long timeMillis, int calendarField, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.roll(calendarField, amount);
        return calendar.getTimeInMillis();
    }

    /**
     * ��ʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩����ȡ��
     * 
     * @param calendar Calendar���͵�ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @return �޸ĺ����Calendar���͵�ʱ��
     */
    public static Calendar ceil(Calendar calendar, int calendarField) {
        DateUtils.modify(calendar, calendarField, CEIL);
        return calendar;
    }

    /**
     * ��ʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩����ȡ��
     * 
     * @param date Date���͵�ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @return �޸ĺ��Date���͵�ʱ��
     */
    public static Date ceil(Date date, int calendarField) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DateUtils.modify(calendar, calendarField, CEIL);
        return calendar.getTime();
    }

    /**
     * ��ʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩����ȡ��
     * 
     * @param timeMillis ��������ʾ��ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @return �޸ĺ���ú�������ʾ��ʱ��
     */
    public static long ceil(long timeMillis, int calendarField) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        DateUtils.modify(calendar, calendarField, CEIL);
        return calendar.getTimeInMillis();
    }

    /**
     * ��ʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩��������
     * 
     * @param calendar Calendar���͵�ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @return �޸ĺ��Calendar���͵�ʱ��
     */
    public static Calendar round(Calendar calendar, int calendarField) {
        DateUtils.modify(calendar, calendarField, ROUND);
        return calendar;
    }

    /**
     * ��ʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩��������
     * 
     * @param date Date���͵�ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @return �޸ĺ��Date���͵�ʱ��
     */
    public static Date round(Date date, int calendarField) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DateUtils.modify(calendar, calendarField, ROUND);
        return calendar.getTime();
    }

    /**
     * ��ʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩��������
     * 
     * @param timeMillis ��������ʾ��ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @return �޸ĺ���ú�������ʾ��ʱ��
     */
    public static long round(long timeMillis, int calendarField) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        DateUtils.modify(calendar, calendarField, ROUND);
        return calendar.getTimeInMillis();
    }

    /**
     * ��ʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩����ȡ��
     * 
     * @param calendar ��Calendar��ʾ��ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @return �޸ĺ���ú�������ʾ��ʱ��
     */
    public static Calendar truncate(Calendar calendar, int calendarField) {
        DateUtils.modify(calendar, calendarField, TRUNCATE);
        return calendar;
    }

    /**
     * ��ʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩����ȡ��
     * 
     * @param date Date���͵�ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @return �޸ĺ����Date���ͱ�ʾ��ʱ��
     */
    public static Date truncate(Date date, int calendarField) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DateUtils.modify(calendar, calendarField, TRUNCATE);
        return calendar.getTime();
    }

    /**
     * ��ʱ���ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩����ȡ��
     * 
     * @param timeMillis �ú�������ʾ��ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @return �޸ĺ���ú�������ʾ��ʱ��
     */
    public static long truncate(long timeMillis, int calendarField) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        DateUtils.modify(calendar, calendarField, TRUNCATE);
        return calendar.getTimeInMillis();
    }

    /**
     * ͨ���������ù���:oper �븨��ʹ��DateUtilHelper��ʹ��
     * 
     * <pre>
     * ��� org.apache.commons.lang.time.DateUtils����Ĺ���:
     *  ceil(����ȡ��),
     *  round(��������),
     *  truncate(����ȡ��)
     * ��CalendarĬ�ϵ�:
     *  set
     *  add
     *  roll
     *  
     * �������,�ṩ����Calendar�Ķ���޸Ŀ�ݷ���,��
     * Calendar c = ...;// ����Ϊ2010-10-25 12:00:02
     * Calendar r = oper(c, 
     *                 DateUtilHelper.add(Calendar.DAY_OF_MONTH, 1),
     *                 DateUtilHelper.set(Calendar.YEAR, 1986),
     *                 DateUtilHelper.truncate(Calendar.MINUTE))
     * 
     * ��r�Ľ���ǣ�1986-10-26 12:00:00
     * </pre>
     * 
     * @param calendar Calendar���͵�ʱ��
     * @param opers ���ɸ�����
     * @return �޸ĺ����Calendar���ͱ�ʾ��ʱ��
     */
    public static Calendar oper(Calendar calendar, long... opers) {
        for (long operMagic : opers) {
            int amount = DateUtilHelper.getAmount(operMagic);
            int operType = DateUtilHelper.getOperType(operMagic);
            int calendarField = DateUtilHelper.getCalendarField(operMagic);
            switch (operType) {
            case SET:
                calendar.set(calendarField, amount);
                break;
            case ADD:
                calendar.add(calendarField, amount);
                break;
            case ROLL:
                calendar.roll(calendarField, amount);
                break;
            case CEIL:
                DateUtils.modify(calendar, calendarField, operType);
                break;
            case ROUND:
                DateUtils.modify(calendar, calendarField, operType);
                break;
            case TRUNCATE:
                DateUtils.modify(calendar, calendarField, operType);
                break;
            }
        }
        return calendar;
    }

    /**
     * ͨ���������ù���:oper �븨��ʹ��DateUtilHelper��ʹ��
     * 
     * @param date Date���͵�ʱ��
     * @param opers ���ɸ�����
     * @return �޸ĺ����Date���ͱ�ʾ��ʱ��
     */
    public static Date oper(Date date, long... opers) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar r = oper(calendar, opers);
        return r.getTime();
    }

    /**
     * ͨ���������ù���:oper �븨��ʹ��DateUtilHelper��ʹ��
     * 
     * @param timeMillis �ú�������ʾ��ʱ��
     * @param opers ���ɸ�����
     * @return �޸ĺ���ú�������ʾ��ʱ��
     */
    public static long oper(long timeMillis, long... opers) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        Calendar r = oper(calendar, opers);
        return r.getTimeInMillis();
    }

    /**
     * ������ʱ���Ӧʱ����λ(TimeUnit)��ʱ��
     * 
     * @param date1
     * @param date2
     * @param timeUnit ʱ�䵥λ
     * @param ceil �Ƿ�����ȡ��
     * @return date1-date2����timeUnitΪ��λ�Ľ����
     */
    public static long getInterval(Date date1, Date date2, TimeUnit timeUnit, boolean ceil) {
        if (timeUnit == TimeUnit.month) { // ���⴦����Ȼ��
            return getMonthsBetween(date1, date2, ceil);
        }
        if (timeUnit == TimeUnit.year) { // ���⴦����Ȼ��
            return getYearsBetween(date1, date2, ceil);
        }
        return (long) UnitConverter.convertTime(date1.getTime() - date2.getTime(), TimeUnit.millisecond, timeUnit, ceil);
    }

    public static long getInterval(Date date1, Date date2, TimeUnit timeUnit) {
        return getInterval(date1, date2, timeUnit, false);
    }

    /**
     * <pre>
     *********** 
     ** private*
     *********** 
     * <pre>
     */
    /**
     * ����ʱ�������������ֵ(��Ȼ��),date1-date2
     * 
     * @param date1
     * @param date2
     * @param ceil true��ʾ ����ȡ��
     * @return date1-date2
     */
    private static long getYearsBetween(Date date1, Date date2, boolean ceil) {
        Calendar c = Calendar.getInstance();
        c.setTime(date1);
        int year1 = c.get(Calendar.YEAR);
        c.set(Calendar.YEAR, DEFAULT_COMPARE_YEAR);
        long time1 = c.getTimeInMillis();

        c.setTime(date2);
        int year2 = c.get(Calendar.YEAR);
        c.set(Calendar.YEAR, DEFAULT_COMPARE_YEAR);
        long time2 = c.getTimeInMillis();

        long result = year1 - year2;
        return ceil(result, time1, time2, ceil);
    }

    /**
     * ����ʱ�������������ֵ(��Ȼ��),date1-date2
     * 
     * @param date1
     * @param date2
     * @param ceil true��ʾ ����ȡ��
     * @return date1-date2
     */
    private static long getMonthsBetween(Date date1, Date date2, boolean ceil) {
        Calendar c = Calendar.getInstance();
        c.setTime(date1);
        int month1 = c.get(Calendar.MONTH);
        int year1 = c.get(Calendar.YEAR);
        c.set(Calendar.MONTH, 0);
        c.set(Calendar.YEAR, DEFAULT_COMPARE_YEAR);
        long time1 = c.getTimeInMillis();

        c.setTime(date2);
        int month2 = c.get(Calendar.MONTH);
        int year2 = c.get(Calendar.YEAR);
        c.set(Calendar.MONTH, 0);
        c.set(Calendar.YEAR, DEFAULT_COMPARE_YEAR);
        long time2 = c.getTimeInMillis();
        // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////>>>>>>>>>>>>>
        long result = (year1 - year2) * 12l + (month1 - month2);
        return ceil(result, time1, time2, ceil);
    }

    /**
     * ����time1��time2֮��Ĳ�ֵ��result��Ӱ�죬�����Ƿ�����ȡ����Ӱ��ļ��㷽ʽ��ͬ
     * 
     * @param result ��Ӱ�����
     * @param time1 ��һ��ʱ��
     * @param time2 �ڶ���ʱ��
     * @param ceil �Ƿ�����ȡ��
     * @return �޸ĺ����
     */
    private static long ceil(long result, long time1, long time2, boolean ceil) {
        long ret = result;
        if (ceil) {
            // ��Ҫ����ȡ��
            if (ret == 0) {
                long diff = time1 - time2;
                if (diff > 0) {
                    ret = 1; // 1
                } else if (diff < 0) {
                    ret = -1; // -1
                }
            }
        } else {
            // ��Ҫ����ȡ��,Ĭ��
            if (ret != 0) {
                long diff = time1 - time2;
                if (diff != 0) {
                    if (ret > 0 && diff < 0) {
                        ret -= 1;
                    } else if (ret < 0 && diff > 0) {
                        ret += 1;
                    }
                }
            }
        }
        return ret;
    }
}
