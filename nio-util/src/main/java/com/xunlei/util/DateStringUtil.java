package com.xunlei.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import com.xunlei.util.UnitConverter.TimeUnit;

/**
 * <pre>
 * ���ִ���򷵻�    ��Ӧpattern�������ַ���  �ķ���
 * 
 * �����¼���飺
 * 1.operͨ�÷���
 * 2.add������ݷ���
 * 3.������date֮�䲻ͬ��λ��ʱ���(getXXXBetween/getInterval)
 * 4.format/parse
 * 
 * </pre>
 * 
 * @author ZengDong
 * @since 2010-10-21 ����05:05:15
 */
public class DateStringUtil {

    private static final int DEFAULT_GETBETWEEN_ERROR_RESULT = Integer.MAX_VALUE;
    private static final Logger log = Log.getLogger();
    private static final ConcurrentHashMap<String, DateStringUtil> allDateStringUtil = new ConcurrentHashMap<String, DateStringUtil>(2);
    /**
     * yyyy-MM-dd HH:mm:ss��ʽ��DateStringUtil
     */
    public static final DateStringUtil DEFAULT_DATE_STRING_UTIL = getInstance(DateUtil.DF_DEFAULT);
    public static final DateStringUtil DEFAULT = DEFAULT_DATE_STRING_UTIL;
    /**
     * yyyy-MM-dd��ʽ��DateStringUtil
     */
    public static final DateStringUtil DEFAULT_DATE_STRING_UTIL_DAY = getInstance(DateUtil.DF_DEFAULT_DAY);
    public static final DateStringUtil DEFAULT_DAY = DEFAULT_DATE_STRING_UTIL_DAY;

    /**
     * ��date��ĳ��ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩����һ���ض���ֵ
     * 
     * @param date Ҫ���ĵ�ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @param amount ���ӵ���
     * @return
     */
    @SuppressWarnings("deprecation")
    public String add(Date date, int calendarField, int amount) {
        if (amount == 0) {
            return getDateFormat().format(date);
        }
        return getDateFormat().format(DateUtils.add(date, calendarField, amount));
    }

    /**
     * ��date��ĳ���ض��㣨�ꡢ�¡��ա�ʱ���֡��룩����һ���ض���ֵ
     * 
     * @param date ���ַ�����ʾ��ʱ��
     * @param calendarField ʱ��㣨�ꡢ�¡��ա�ʱ���֡��룩
     * @param amount ���ӵ���
     * @return
     */
    @SuppressWarnings("deprecation")
    public String add(String date, int calendarField, int amount) {
        try {
            if (amount == 0) {
                return date;
            }
            DateFormat df = getDateFormat();
            return df.format(DateUtils.add(df.parse(date), calendarField, amount));
        } catch (Exception e) {
            log.error(dateForamtPattern, e);
            return date;
        }
    }

    /**
     * �ڽ���������������������ʱ���ַ���
     * 
     * @param year Ҫ���ӵ�����
     * @return
     */
    public String addYears(int year) {
        return addYears(new Date(), year);
    }

    /**
     * ��ָ����ʱ��date�������������������ʱ���ַ���
     * 
     * @param date
     * @param year
     * @return
     */
    public String addYears(Date date, int year) {
        return add(date, Calendar.YEAR, year);
    }

    /**
     * �����ַ�����ʾ��ʱ��date��������������µ�ʱ���ַ���
     * 
     * @param date �ַ�����ʾ��ʱ��
     * @param year Ҫ���ӵ�����
     * @return
     */
    public String addYears(String date, int year) {
        return add(date, Calendar.YEAR, year);
    }

    /**
     * �ڽ���Ļ������������������ʱ���ַ���
     * 
     * @param month
     * @return
     */
    public String addMonths(int month) {
        return addMonths(new Date(), month);
    }

    /**
     * ��date�Ļ������������������ʱ���ַ���
     * 
     * @param date
     * @param month
     * @return
     */
    public String addMonths(Date date, int month) {
        return add(date, Calendar.MONTH, month);
    }

    /**
     * �����ַ�����ʾ��ʱ��date���������������ʱ���ַ���
     * 
     * @param date
     * @param month
     * @return
     */
    public String addMonths(String date, int month) {
        return add(date, Calendar.MONTH, month);
    }

    /**
     * �ڽ����ʱ����������������������ʱ���ַ���
     * 
     * @param week
     * @return
     */
    public String addWeeks(int week) {
        return addWeeks(new Date(), week);
    }

    /**
     * ��date�Ļ������������ں����ʱ���ַ���
     * 
     * @param date
     * @param week
     * @return
     */
    public String addWeeks(Date date, int week) {
        return add(date, Calendar.WEEK_OF_YEAR, week);
    }

    /**
     * �����ַ�����ʾ��ʱ��date���������ں����ʱ���ַ���
     * 
     * @param date
     * @param week
     * @return
     */
    public String addWeeks(String date, int week) {
        return add(date, Calendar.WEEK_OF_YEAR, week);
    }

    /**
     * �ڽ����ʱ��������������������ʱ���ַ���
     * 
     * @param day
     * @return
     */
    public String addDays(int day) {
        return addDays(new Date(), day);
    }

    /**
     * ��date�Ļ������������������ʱ���ַ���
     * 
     * @param date
     * @param day
     * @return
     */
    public String addDays(Date date, int day) {
        return add(date, Calendar.DAY_OF_MONTH, day);
    }

    /**
     * �����ַ�����ʾ��date���������������ʱ���ַ���
     * 
     * @param date
     * @param day
     * @return
     */
    public String addDays(String date, int day) {
        return add(date, Calendar.DAY_OF_MONTH, day);
    }

    /**
     * �����ڵ�ʱ�����������Сʱ�������ʱ���ַ���
     * 
     * @param hour
     * @return
     */
    public String addHours(int hour) {
        return addHours(new Date(), hour);
    }

    /**
     * ��date��ʱ�����������Сʱ�������ʱ���ַ���
     * 
     * @param date
     * @param hour
     * @return
     */
    public String addHours(Date date, int hour) {
        return add(date, Calendar.HOUR_OF_DAY, hour);
    }

    /**
     * �����ַ�����ʾ��ʱ��date������Сʱ�������ʱ���ַ���
     * 
     * @param date
     * @param hour
     * @return
     */
    public String addHours(String date, int hour) {
        return add(date, Calendar.HOUR_OF_DAY, hour);
    }

    /**
     * �����ڵ�ʱ����������ӷ����������ʱ���ַ���
     * 
     * @param miniute
     * @return
     */
    public String addMiniutes(int miniute) {
        return addMiniutes(new Date(), miniute);
    }

    /**
     * ��date��ʱ����������ӷ����������ʱ���ַ���
     * 
     * @param date
     * @param miniute
     * @return
     */
    public String addMiniutes(Date date, int miniute) {
        return add(date, Calendar.MINUTE, miniute);
    }

    /**
     * �����ַ�����ʾ��ʱ��date�����ӷ����������ʱ���ַ���
     * 
     * @param date
     * @param miniute
     * @return
     */
    public String addMiniutes(String date, int miniute) {
        return add(date, Calendar.MINUTE, miniute);
    }

    /**
     * �����ڵ�ʱ��������������������ʱ���ַ���
     * 
     * @param second
     * @return
     */
    public String addSeconds(int second) {
        return addSeconds(new Date(), second);
    }

    /**
     * ��date��ʱ��������������������ʱ���ַ���
     * 
     * @param date
     * @param second
     * @return
     */
    public String addSeconds(Date date, int second) {
        return add(date, Calendar.SECOND, second);
    }

    /**
     * �����ַ�����ʾ��ʱ��date���������������ʱ���ַ���
     * 
     * @param date
     * @param second
     * @return
     */
    public String addSeconds(String date, int second) {
        return add(date, Calendar.SECOND, second);
    }

    /**
     * ����ʱ�������������ֵ(��Ȼ��),date1-date2
     * 
     * @param date1
     * @param date2
     * @return
     */
    public static long getYearsBetween(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.year, false);
    }

    /**
     * ����ʱ�������������ֵ(��Ȼ��),date1-date2
     * 
     * @param date1
     * @param date2
     * @return
     */
    public long getYearsBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.year, false);
    }

    /**
     * ����ʱ�������������ֵ(��Ȼ��),������ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return
     */
    public static long getYearsBetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.year, true);
    }

    /**
     * ����ʱ�������������ֵ(��Ȼ��),������ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return
     */
    public long getYearsBetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.year, true);
    }

    /**
     * ����ʱ�������������ֵ(����Ȼ��,��365������),date1-date2
     * 
     * @param date1
     * @param date2
     * @return
     */
    public static long getYears365Between(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.year365, false);
    }

    /**
     * ����ʱ�������������ֵ(����Ȼ��,��365������),date1-date2
     * 
     * @param date1
     * @param date2
     * @return
     */
    public long getYears365Between(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.year365, false);
    }

    /**
     * ����ʱ�������������ֵ(����Ȼ��,��365������),����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     */
    public static long getYears365BetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.year365, true);
    }

    /**
     * ����ʱ�������������ֵ(����Ȼ��,��365������),����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     */
    public long getYears365BetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.year365, true);
    }

    /**
     * ����ʱ�������������ֵ(��Ȼ��),date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMonthsBetween(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.month, false);
    }

    /**
     * ����ʱ�������������ֵ(��Ȼ��),date1-date2
     * 
     * @return date1-date2
     */
    public long getMonthsBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.month, false);
    }

    /**
     * ����ʱ�������������ֵ(��Ȼ��),������ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMonthsBetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.month, true);
    }

    /**
     * ����ʱ�������������ֵ(��Ȼ��),������ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public long getMonthsBetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.month, true);
    }

    /**
     * ����ʱ�������������ֵ(����Ȼ��,��31������),date1-date2O
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMonths31Between(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.month31, false);
    }

    /**
     * ����ʱ�������������ֵ(����Ȼ��,��31������),date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public long getMonths31Between(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.month31, false);
    }

    /**
     * ����ʱ�������������ֵ(����Ȼ��,��31������),����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMonths31BetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.month31, true);
    }

    /**
     * ����ʱ�������������ֵ(����Ȼ��,��31������),����ȡ��,date1-date2
     * 
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public long getMonths31BetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.month31, true);
    }

    /**
     * ����ʱ�������������ֵ(����Ȼ��,��30������),date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMonths30Between(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.month30, false);
    }

    /**
     * ����ʱ�������������ֵ(����Ȼ��,��30������),date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     */
    public long getMonths30Between(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.month30, false);
    }

    /**
     * ����ʱ�������������ֵ(����Ȼ��,��30������),����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMonths30BetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.month30, true);
    }

    /**
     * ����ʱ�������������ֵ(����Ȼ��,��30������),����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     */
    public long getMonths30BetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.month30, true);
    }

    /**
     * ����ʱ�������������ֵ(��Ȼ��,��7������),date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getWeeksBetween(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.week, false);
    }

    /**
     * ����ʱ�������������ֵ(��Ȼ��,��7������),date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     */
    public long getWeeksBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.week, false);
    }

    /**
     * ����ʱ�������������ֵ(��Ȼ��,��7������),����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getWeeksBetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.week, true);
    }

    /**
     * ����ʱ�������������ֵ(��Ȼ��,��7������),����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     */
    public long getWeeksBetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.week, true);
    }

    /**
     * ����ʱ�������������ֵ,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getDaysBetween(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.day, false);
    }

    /**
     * ����ʱ�������������ֵ,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     */
    public long getDaysBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.day, false);
    }

    /**
     * ����ʱ�������������ֵ,����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getDaysBetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.day, true);
    }

    /**
     * ����ʱ�������������ֵ,����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     */
    public long getDaysBetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.day, true);
    }

    /**
     * ����ʱ�������Сʱ��ֵ,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getHoursBetween(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.hour, false);
    }

    /**
     * ����ʱ�������Сʱ��ֵ,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     */
    public long getHoursBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.hour, false);
    }

    /**
     * ����ʱ�������Сʱ��ֵ,����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getHoursBetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.hour, true);
    }

    /**
     * ����ʱ�������Сʱ��ֵ,����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     */
    public long getHoursBetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.hour, true);
    }

    /**
     * ����ʱ������ķ��Ӳ�ֵ,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMinutesBetween(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.minute, false);
    }

    /**
     * ����ʱ������ķ��Ӳ�ֵ,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     */
    public long getMinutesBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.minute, false);
    }

    /**
     * ����ʱ������ķ��Ӳ�ֵ,����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMinutesBetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.minute, true);
    }

    /**
     * ����ʱ������ķ��Ӳ�ֵ,����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     */
    public long getMinutesBetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.minute, true);
    }

    /**
     * ����ʱ�������������ֵ,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getSecondsBetween(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.second, false);
    }

    /**
     * ����ʱ�������������ֵ,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     */
    public long getSecondsBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.second, false);
    }

    /**
     * ����ʱ�������������ֵ,����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getSecondsBetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.second, true);
    }

    /**
     * ����ʱ�������������ֵ,����ȡ��,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2,�������ʧ��,����Integer.MAX_VALUE
     */
    public long getSecondsBetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.second, true);
    }

    /**
     * ����ʱ������ĺ�������ֵ,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMillisecondsBetween(Date date1, Date date2) {
        return date1.getTime() - date2.getTime();
    }

    /**
     * ����ʱ������ĺ�������ֵ,date1-date2
     * 
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public long getMillisecondsBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.millisecond, false);
    }

    /**
     * ��һ���µ�ʱ��
     * 
     * @return
     */
    public String lastMonthToday() {
        return addMonths(-1);
    }

    /**
     * �ϸ����ڵ�ʱ��
     * 
     * @return
     */
    public String lastWeekToday() {
        return addWeeks(-1);
    }

    /**
     * �¸��µ�ʱ��
     * 
     * @return
     */
    public String nextMonthToday() {
        return addMonths(1);
    }

    /**
     * �¸����ڵ�ʱ��
     * 
     * @return
     */
    public String nextWeekToday() {
        return addWeeks(1);
    }

    /**
     * ��ȡ�����ʱ��
     * 
     * @return
     */
    public String tomorrow() {
        return addDays(1);
    }

    /**
     * ��ȡ�����ʱ��
     * 
     * @return
     */
    public String yesterday() {
        return addDays(-1);
    }

    /**
     * ��ȡ���ڵ�ʱ��
     * 
     * @return
     */
    public String now() {
        return dateFormatUnsafe.format(System.currentTimeMillis());
    }

    /**
     * ��ȡ����ʱ��֮���������ʱ��
     * 
     * @param second
     * @return
     */
    public String afterNow(long second) {
        return getDateFormat().format(System.currentTimeMillis() + second * 1000);
    }

    /**
     * ��ȡ����ʱ��֮ǰ�������ʱ��
     * 
     * @param second
     * @return
     */
    public String beforeNow(long second) {
        return afterNow(-second);
    }

    /**
     * ��unixʱ��ת����ָ��pattern���ַ���
     */
    public String unix2String(long unixTime) {
        return getDateFormat().format(new Date(unixTime * 1000));
    }

    /**
     * ���ַ���ʱ��ת����unixʱ��
     */
    public long string2Unix(String dateStr) throws Exception {
        Date date = getDateFormat().parse(dateStr);
        return date.getTime() / 1000;
    }

    /**
     * ��ʽ��ʱ���yyyy-MM-dd HH:mm:ss
     * 
     * @param date
     * @return
     */
    public String format(Date date) {
        return getDateFormat().format(date);
    }

    /**
     * ����yyyy-MM-dd HH:mm:ss��yyyy-MM-dd��ʽ
     * 
     * @param dateStr
     * @return
     */
    public Date parse(String dateStr) {
        try {
            return getDateFormat().parse(dateStr);
        } catch (Exception e) {
            log.error(dateForamtPattern, e);
            return null;
        }
    }

    /**
     * �����ַ�������ͨ�÷���
     * 
     * @param calendar
     * @param opers
     * @return
     */
    public String oper(Calendar calendar, long... opers) {
        return format(DateUtil.oper(calendar, opers).getTime());
    }

    /**
     * �����ַ�������ͨ�÷���
     * 
     * @param date
     * @param opers
     * @return
     */
    public String oper(Date date, long... opers) {
        return format(DateUtil.oper(date, opers));
    }

    /**
     * �����ַ�������ͨ�÷���
     * 
     * @param dateStr ������yyyy-MM-dd HH:mm:ss��yyyy-MM-dd��ʽ
     * @param opers ������,ʹ��DateUtil.add(),set(),roll(),ceil(),round(),truncate()����������
     * @return ������Ľ���ַ���
     */
    public String oper(String dateStr, long... opers) {
        try {
            DateFormat df = getDateFormat();
            Date d = DateUtil.oper(df.parse(dateStr), opers);
            return df.format(d);
        } catch (Exception e) {
            log.error(dateForamtPattern, e);
            return dateStr;
        }
    }

    /**
     * ʱ���ʽ
     */
    private final String dateForamtPattern;
    /**
     * ʱ���ʽ����
     */
    private final ThreadLocal<DateFormat> dateFormatFactory;
    /**
     * ����ȫ��ʱ���ʽ
     */
    private final DateFormat dateFormatUnsafe;

    /**
     * ˽�еĹ��췽��
     * 
     * @param dateForamtPattern
     */
    private DateStringUtil(String dateForamtPattern) {
        this.dateForamtPattern = dateForamtPattern;
        this.dateFormatFactory = DateUtil.makeDateFormatPerThread(dateForamtPattern);
        this.dateFormatUnsafe = new SimpleDateFormat(dateForamtPattern);
    }

    /**
     * ���ʱ���ʽ����
     * 
     * @return
     */
    public DateFormat getDateFormat() {
        return dateFormatFactory.get();
    }

    /**
     * ���ʱ���ʽ
     * 
     * @return
     */
    public String getDateForamtPattern() {
        return dateForamtPattern;
    }

    /**
     * ���date1��date2֮��Ĳ�ֵ����timeUnit��ʾ�����������Ƿ�����ȡ��
     * 
     * @param date1
     * @param date2
     * @param timeUnit ��ֵ�ĵ�λ
     * @param ceil �Ƿ�����ȡ��
     * @return date1-date2
     */
    public long getInterval(String date1, String date2, TimeUnit timeUnit, boolean ceil) {
        DateFormat df = getDateFormat();
        try {
            return DateUtil.getInterval(df.parse(date1), df.parse(date2), timeUnit, ceil);
        } catch (Exception e) {
            log.error(dateForamtPattern, e);
            return DEFAULT_GETBETWEEN_ERROR_RESULT;
        }
    }

    /**
     * ���date1��date2֮��Ĳ�ֵ����timeUnit��ʾ�����������Ƿ�����ȡ��
     * 
     * @param date1
     * @param date2
     * @param timeUnit ��ֵ�ĵ�λ
     * @param ceil �Ƿ�����ȡ��
     * @return date1-date2
     */
    public static long getInterval(Date date1, Date date2, TimeUnit timeUnit, boolean ceil) {
        return DateUtil.getInterval(date1, date2, timeUnit, ceil);
    }

    /**
     * DateStringUtil���칤������
     * 
     * @param dateForamtPattern
     * @return
     */
    public static DateStringUtil getInstance(String dateForamtPattern) {
        DateStringUtil dsu = allDateStringUtil.get(dateForamtPattern);
        if (dsu == null) {
            dsu = new DateStringUtil(dateForamtPattern);
            allDateStringUtil.putIfAbsent(dateForamtPattern, dsu);
        }
        return dsu;
    }

}
