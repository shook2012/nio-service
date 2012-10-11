package com.xunlei.netty.httpserver.cmd.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xunlei.spring.AfterConfig;
import com.xunlei.util.DateStringUtil;
import com.xunlei.util.concurrent.ConcurrentUtil;

/**
 * <pre>
 * �ṩ��httpServer��tps�ļ�ʵʱͳ��
 * 
 * ע�⣺���stat������,���Ƕ�ʱ���tps����ʾ�ɸ���,�������������
 * 
 * @author ZengDong
 * @since 2011-1-15 ����06:39:05
 */
@Service
public class StatisticsHelper {

    // private static final Logger log = Log.getLogger();
    /**
     * ������
     */
    public class Snapshot {

        private Date date;// ͳ��ʱ��ǰʱ��
        private long num;// ͳ��ʱ��ǰ��Ŀ

        private Snapshot(long num) {
            this.date = new Date();
            this.num = num;
        }

        private Snapshot(Date date, long num) {
            this.date = date;
            this.num = num;
        }

        public Date getDate() {
            return date;
        }

        public long getNum() {
            return num;
        }

        @Override
        public String toString() {
            return DateStringUtil.DEFAULT.format(date) + "   " + num;
        }
    }

    @AfterConfig
    public synchronized void init() {
        if (init) {
            return;
        }
        maxTps = snapshotArray[snapshotIndex++ % snapshotSize] = new Snapshot(0);// ���п���ע�͵�,ע�͵��Ļ���û�м�¼����0��¼�����
        ConcurrentUtil.getDaemonExecutor().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    Snapshot dss = new Snapshot(statCmd.getProcessTSS().getAllNum());
                    snapshotArray[snapshotIndex++ % snapshotSize] = dss;
                    // log.error(dss.toString());
                    Snapshot dss0 = snapshotArray[snapshotIndex - 2 % snapshotSize];// ��һ��ʱ,�ᱨ��,����
                    lastTps = calcTps(dss, dss0);
                    if (maxTps.getNum() <= lastTps) {
                        maxTps = new Snapshot(dss.getDate(), lastTps);
                    }
                } catch (Exception e) {
                    // log.error("", e);
                }
            }
        }, getSnapshotInitialDelay(), snapshotMilliseconds, TimeUnit.MILLISECONDS);
        init = true;
    }

    private boolean init;
    @Autowired
    private StatCmd statCmd;
    private static final int dayms = 3600 * 1000 * 24;
    // @Config
    private int snapshotDay = 30; // �ڴ��¼�¼���Ŀ��ռ�¼
    private int snapshotMilliseconds = 600000;// ÿ��10min��¼һ��
    // private int snapshotMilliseconds = 10000;
    private int snapshotSize = dayms / snapshotMilliseconds * snapshotDay;// ÿ��10min��¼һ��,����ÿСʱ����6����,�ڴ��¼30��ļ�¼
    private Snapshot[] snapshotArray = new Snapshot[snapshotSize];
    private int snapshotIndex = 0;
    private Snapshot maxTps;// ��ʷ��߼�¼
    private long lastTps = 0;// �ϴ�ͳ�Ƶ���tps

    private long getSnapshotInitialDelay() {// ʵ��������ϵͳʱ��� ����ʱ�书��
        String truncateDateStr = DateStringUtil.DEFAULT.now().substring(0, 15) + "0:00";// ����ÿ��10min
        // String truncateDateStr = DateStringUtil.DEFAULT.now().substring(0, 18) + "0";// ����ÿ��10sec
        Date truncateDate = DateStringUtil.DEFAULT.parse(truncateDateStr);
        return truncateDate.getTime() + snapshotMilliseconds - System.currentTimeMillis();
    }

    /**
     * @param sampleSize Ҫ����tps�Ĳ�����
     * @param interval �������������(С��snapshotMillisecondsʱ��snapshotMilliseconds����)
     * @param calcTps true��ʾ����tps,false��ʾ��ʾԭʼ����
     */
    public List<Snapshot> getSnapshot(int sampleSize, long interval, boolean calcTps) {
        int currentSnapshotIndex = snapshotIndex;// ��Ϊ�ڲ�ѯ��ǰ���ʱ,��������Ļ�snapshotIndexһֱ�ڵ���,����Ҫ�ȱ������idx
        int step = (int) (interval / snapshotMilliseconds);// �����������
        if (step <= 0)
            step = 1;

        int size = sampleSize * step;// sizeָ����������ص����ݵ��ܿ��
        if (size > snapshotSize)
            size = snapshotSize;
        int beginIndex = currentSnapshotIndex - size;

        List<Snapshot> statList = new ArrayList<Snapshot>();
        if (calcTps) {
            if (beginIndex < step)
                beginIndex = step;
            for (int i = beginIndex; i < currentSnapshotIndex; i = i + step) {
                Snapshot dss0 = snapshotArray[(i - step) % snapshotSize];
                Snapshot dss = snapshotArray[i % snapshotSize];
                statList.add(new Snapshot(dss.getDate(), calcTps(dss, dss0)));
            }
        } else {
            if (beginIndex < 0)
                beginIndex = 0;
            for (int i = beginIndex; i < currentSnapshotIndex; i = i + step) {
                Snapshot dss = snapshotArray[i % snapshotSize];
                statList.add(dss);
            }
        }
        return statList;
    }

    private static long calcTps(Snapshot dss, Snapshot dss0) {
        long num = dss.getNum() - dss0.getNum();
        long sec = (dss.getDate().getTime() - dss0.getDate().getTime()) / 1000;
        if (sec <= 0)
            return 0;
        return num / sec;
    }

    public Snapshot getMaxTps() {
        return maxTps;
    }

    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < snapshotIndex; i++) {
            if (snapshotArray[i] == null)
                break;
            tmp.append(snapshotArray[i]).append("\n");
        }
        return tmp.toString();
    }
}
