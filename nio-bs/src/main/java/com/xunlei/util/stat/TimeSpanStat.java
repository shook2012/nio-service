package com.xunlei.util.stat;

import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import com.xunlei.util.HumanReadableUtil;

/**
 * 时长统计
 * 
 * @author ZengDong
 * @since 2010-5-30 上午02:14:24
 */
public class TimeSpanStat {

    protected AtomicLong all_num = new AtomicLong();
    protected AtomicLong all_span = new AtomicLong();
    protected Logger log;
    protected volatile long max_span;
    protected String name;
    protected AtomicLong slow_num = new AtomicLong();
    protected AtomicLong slow_span = new AtomicLong();
    protected int slowThreshold;
    protected String tableHeader;
    protected String timeStatFmt;
    protected boolean warn;

    public TimeSpanStat(String name, int slowThreshold, boolean warn, Logger log) {
        this.name = name;
        this.slowThreshold = slowThreshold;
        this.log = log;
        this.warn = warn;
        initFormat(35, 0);
    }

    public TimeSpanStat(String name, Logger log) {
        this(name, 1000, true, log);
    }

    public long getAllNum() {
        return all_num.get();
    }

    public long getAllSpan() {
        return all_span.get();
    }

    public long getSlowNum() {
        return slow_num.get();
    }

    public long getSlowSpan() {
        return slow_span.get();
    }

    public String getTableHeader() {
        return tableHeader;
    }

    public void initFormat(int nameLen, int nameFullWidthCharNum) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nameFullWidthCharNum; i++) {
            sb.append("　");
        }
        this.timeStatFmt = "%-" + nameLen + "s %-8s %-20s %-8s %-20s %-20s %-20s %-20s\n";
        this.tableHeader = String.format(timeStatFmt, sb.toString(), "times", "avg", "slow", "slow_avg", "max", "slow_span", "all_span");
    }

    public void record(long end, long begin, Object arg) {
        if (begin <= 0 || end <= 0) {
            return;
        }
        all_num.incrementAndGet();
        long span = end - begin;
        all_span.addAndGet(span);
        if (span >= slowThreshold) {
            slow_num.incrementAndGet();
            slow_span.addAndGet(span);
            if (warn) {
                warn(end, begin, arg);
            }
        }
        if (span > max_span) {
            max_span = span;
        }
    }

    public void record(long end, long begin, int count, Object arg) {
        if (begin <= 0 || end <= 0) {
            return;
        }
        all_num.addAndGet(count);
        long span = end - begin;
        all_span.addAndGet(span);
        if (span / count >= slowThreshold) {
            slow_num.addAndGet(count);
            slow_span.addAndGet(span);
            if (warn) {
                warn(end, begin, arg);
            }
        }
        if (span > max_span) {
            max_span = span;
        }
    }

    @Override
    public String toString() {
        return toString(timeStatFmt, name);
    }

    public String toString(String first) {
        return toString(timeStatFmt, first);
    }

    public String toString(String timeStatFmt, String first) {
        long all_numTMP = all_num.get();
        long all_spanTMP = all_span.get();
        long slow_numTMP = slow_num.get();
        long slow_spanTMP = slow_span.get();

        long allAvg = all_numTMP > 0 ? all_spanTMP / all_numTMP : 0;
        long slowAvg = slow_numTMP > 0 ? slow_spanTMP / slow_numTMP : 0;

        return String.format(timeStatFmt, first, all_numTMP > 0 ? all_numTMP : "", HumanReadableUtil.timeSpan(allAvg), slow_numTMP > 0 ? slow_numTMP : "", HumanReadableUtil.timeSpan(slowAvg),
                HumanReadableUtil.timeSpan(max_span), HumanReadableUtil.timeSpan(slow_spanTMP), HumanReadableUtil.timeSpan(all_spanTMP));
    }

    protected void warn(long end, long begin, Object arg) {
        log.error("SLOW_PROCESS:{}:{} [{}ms]\n", new Object[] { name, arg, end - begin });
    }

    public boolean isNeedReset() {
        return all_num.get() < 0 || all_span.get() < 0;
    }
}
