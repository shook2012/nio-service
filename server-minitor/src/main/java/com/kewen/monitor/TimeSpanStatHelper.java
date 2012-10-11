package com.kewen.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kewen.monitor.googlechart.ChartAxis;
import com.kewen.monitor.googlechart.charts.LineChart;
import com.kewen.monitor.googlechart.data.ChartData;
import com.kewen.monitor.googlechart.data.VectorInt;
import com.xunlei.spring.AfterBootstrap;
import com.xunlei.spring.AfterConfig;
import com.xunlei.spring.Config;
import com.xunlei.util.CircularQueue;
import com.xunlei.util.DateStringUtil;
import com.xunlei.util.HumanReadableUtil;
import com.xunlei.util.concurrent.BaseSchedulable;
import com.xunlei.util.stat.TimeSpanStat;

@Service
public class TimeSpanStatHelper {

    private Map<TimeSpanStat, TimeSpanStatSbapshotQueue> queueMap = new LinkedHashMap<TimeSpanStat, TimeSpanStatSbapshotQueue>();
    @Config(resetable = true)
    private long timeSpanStatSnapshotSec = 10 * 60;// 每十分钟统计一次
    @Config
    private int timeSpanStatQueueSize = 24 * 6;// 默认保存一天的记录 （10分钟 * 6 * 24 刚好是一天的跨度）

    private BaseSchedulable sche = new BaseSchedulable() {

        @Override
        public void process() throws Throwable {
            for (TimeSpanStatSbapshotQueue q : queueMap.values()) {
                q.snapshot();
            }
        }
    };

    @Autowired
    private StatCmd statCmd;

    @AfterConfig
    protected void init() {
        sche.scheduleAtFixedRateSec(timeSpanStatSnapshotSec);
    }

    @AfterBootstrap
    protected void defaultRegister() {
        register(statCmd.getProcessTSS(), "HTTP请求");
    }

    public void register(TimeSpanStat tss, String name) {
        queueMap.put(tss, new TimeSpanStatSbapshotQueue(name, tss, timeSpanStatQueueSize + 1));// 因为是end-being ，所以其实是要+1
    }

    public void unregister(TimeSpanStat tss) {
        queueMap.remove(tss);
    }

    // http://www.haijd.net/archive/computer/google/google_chart_api/api.html
    // http://javagooglechart.googlecode.com/
    private static String getChartUrl(List<TimeSpanStatResult> result) {
        int size = result.size();
        if (size < 3) // 没有必要画图
            return "";

        int[] timesArray = new int[size];
        int[] tpsArray = new int[size];
        int[] avgArray = new int[size];
        int[] slowArray = new int[size];

        // Each dataset is the set of y-axis coordinates
        // they will be spaced evenly on the x-axis
        for (int i = 0; i < size; i++) {
            TimeSpanStatResult r = result.get(size - 1 - i);
            tpsArray[i] = (int) r.getTps();
            avgArray[i] = (int) r.getAllAvg();
            slowArray[i] = (int) r.slow_num;
            timesArray[i] = (int) r.all_num;
        }

        int bottomSize = Math.min(18, size);// 找了 144 的倍数
        String[] bottom = new String[bottomSize];
        for (int i = 1; i <= bottom.length; i++) {
            bottom[i - 1] = i * size / bottomSize + "";
        }
        if (size / bottomSize > 1) {
            String[] bottom1 = new String[bottom.length + 1];
            bottom1[0] = "1";
            System.arraycopy(bottom, 0, bottom1, 1, bottom.length);
            bottom = bottom1;
        }

        ChartAxis bottomAxis = new ChartAxis(ChartAxis.Bottom, bottom);

        StringBuilder urls = new StringBuilder();
        urls.append(buildChartUrl("times", timesArray, bottomAxis));
        urls.append(buildChartUrl("tps", tpsArray, bottomAxis));
        urls.append(buildChartUrl("avg", avgArray, bottomAxis));
        urls.append(buildChartUrl("slow", slowArray, bottomAxis));

        return urls.toString();
    }

    @SuppressWarnings("unchecked")
    private static String buildChartUrl(String title, int[] array, ChartAxis bottomAxis) {
        boolean needPaint = false;
        for (int a : array) {
            if (a > 0) {
                needPaint = true;
                break;
            }
        }
        if (!needPaint)
            return "";

        // Create a line chart that is 250 pixels wide and 150 pixels high
        LineChart lineChart = new LineChart(1000, 200);
        // If we create an axis with only this parameter it will
        // have a range of 0-100 and be evenly spaced across the chart
        // Set the title text, title color and title font size in pixels
        lineChart.setTitle(title, "0000FF", 14);
        VectorInt datasets = new VectorInt();
        datasets.add(array);
        // Set the chart to use our collection of datasets
        lineChart.setData(datasets);
        lineChart.setDatasetColors(new String[] { "76A4FB" });

        lineChart.addAxis(bottomAxis);

        lineChart.addAxis(new ChartAxis(ChartAxis.Left, new String[] { "", ChartData.findMaxValue(array) + "" }));

        String url = lineChart.getUrl();
        return "<img src=\"" + url + "\" alt=\"" + title + "\">";
    }

    public String getInfo() {
        String fmt = "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n";

        StringBuilder tmp = new StringBuilder();
        for (TimeSpanStatSbapshotQueue q : queueMap.values()) {
            List<TimeSpanStatResult> result = q.getResult();
            tmp.append("\n").append(getChartUrl(result)).append("\n\n");
            tmp.append("<table width=\"70%\"><tbody>\n");
            tmp.append(String.format(fmt, "", q.name, "times", "avg", "slow", "slow_avg", "tps"));
            int i = result.size();
            for (TimeSpanStatResult tssr : result) {
                tmp.append(tssr.toString(fmt, i--));
            }
            tmp.append("</tbody></table>");

            // StringHelper.printLine(tmp, 200, '-');
        }
        return tmp.toString();
    }

    private static String timeSpanStatResultFmt = "%-19s %-8s %-20s %-8s %-20s %-8s\n";

    public static class TimeSpanStatResult {

        protected long ts;
        protected long span;

        protected long all_num;
        protected long all_span;
        protected long slow_num;
        protected long slow_span;

        public TimeSpanStatResult(TimeSpanStatSnapshot end, TimeSpanStatSnapshot begin) {
            this.ts = end.ts;
            this.span = end.ts - begin.ts;

            this.all_num = end.all_num - begin.all_num;
            this.all_span = end.all_span - begin.all_span;

            this.slow_num = end.slow_num - begin.slow_num;
            this.slow_span = end.slow_span - begin.slow_span;
        }

        public long getTps() {
            return all_num *1000 / span;
        	//return all_num / span / 1000;
        }

        public long getAllAvg() {
            return all_num > 0 ? all_span / all_num : 0;
        }

        public long getSlowAvg() {
            return slow_num > 0 ? slow_span / slow_num : 0;
        }

        public String toString() {
            return String.format(timeSpanStatResultFmt, DateStringUtil.DEFAULT.format(new Date(ts)), all_num, HumanReadableUtil.timeSpan(getAllAvg()), slow_num,
                    HumanReadableUtil.timeSpan(getSlowAvg()), getTps());
        }

        public String toString(String fmt, int idx) {
            return String.format(fmt, idx, DateStringUtil.DEFAULT.format(new Date(ts)), all_num, HumanReadableUtil.timeSpan(getAllAvg()), slow_num, HumanReadableUtil.timeSpan(getSlowAvg()), getTps());
        }
    }

    public static class TimeSpanStatSnapshot {

        protected long ts;

        protected long all_num;
        protected long all_span;
        protected long slow_num;
        protected long slow_span;

        public TimeSpanStatSnapshot(long ts, TimeSpanStat tss) {
            this.ts = ts;
            this.all_num = tss.getAllNum();
            this.all_span = tss.getAllSpan();
            this.slow_num = tss.getSlowNum();
            this.slow_span = tss.getSlowSpan();
        }

    }

    public static class TimeSpanStatSbapshotQueue {

        private String name;
        private TimeSpanStat timeSpanStat;
        private CircularQueue<TimeSpanStatSnapshot> queue;

        public TimeSpanStatSbapshotQueue(String name, TimeSpanStat tss, int queueSize) {
            this.name = name;
            this.timeSpanStat = tss;
            this.queue = new CircularQueue<TimeSpanStatHelper.TimeSpanStatSnapshot>(queueSize);
            this.snapshot();// 初始化时，就先进一个
        }

        public void snapshot() {
            queue.addToHead(new TimeSpanStatSnapshot(System.currentTimeMillis(), timeSpanStat));
        }

        public List<TimeSpanStatResult> getResult() {
            List<TimeSpanStatResult> list = new ArrayList<TimeSpanStatHelper.TimeSpanStatResult>();
            Iterator<TimeSpanStatSnapshot> i = queue.iterator();
            if (i.hasNext()) {
                TimeSpanStatSnapshot end = i.next();
                while (i.hasNext()) {
                    TimeSpanStatSnapshot begin = i.next();
                    list.add(new TimeSpanStatResult(end, begin));
                    end = begin;
                }
            }
            return list;
        }
    }

};
