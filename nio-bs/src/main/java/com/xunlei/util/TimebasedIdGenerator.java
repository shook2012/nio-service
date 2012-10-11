package com.xunlei.util;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;

/**
 * <pre>
 *  >>>����ʱ�����Long������������<<<
 * 
 * �����д�ʱ���(���뾫ȷ����)��long��Ψһ���к�,��̳���Ϊ12λ,�����Ϊ19λ
 *  Long.MAX_VALUE = 9223372036854775807 [19λ]
 *  
 *  [yy]yyMMddHHmmss ��Ĭ�ϲ��ɸĵ�������ʱ����(fullYearMode=trueʱ,ʹ��yyyy������)
 *  accuracyLen      ��ʾ���ȱ��λ��
 *  incrLen          ��ʾ�ڲ�������λ��
 *  
 *  �������к� = [yy]yyMMddHHmmss + accuracyLen(����incrLen)
 *  accuracyLen-incrLen=0 ��ʾ��ȷ����
 *  accuracyLen-incrLen=3 ��ʾ��ȷ������
 *  accuracyLen-incrLen=6 ��ʾ��ȷ��΢��  
 *  accuracyLen-incrLen=9 ��ʾ��ȷ������(��Ϊ12+9 > 19 �Ѿ�����long��,���Ծ�ȷ��������)
 *  
 * ʹ��     yyMMddHHmmss��ʾʱ,��ﵽ���Ѿ�ռ����12λ,��˻���7λ����,Ҳ��������߾��ȿɴﵽ 1/ 10 000 000 (7����) ��,��:ǧ���֮һ��
 * ʹ��yyyyMMddHHmmss��ʾʱ,��ﵽ���Ѿ�ռ����14λ,��˻���5λ����,Ҳ��������߾��ȿɴﵽ 1/ 100 000 (5����) ��,   ��:ʮ���֮һ��
 * 
 * ע��:yyMMddHHmmssֻ�ܱ�ﵽ2092��,����2093���long�����
 * 
 * �������³�����
 * 1.���ʹ��yyMMddHHmmss,��accuracyLen=0,incrLen=0,Ҳ����ֻ��ȷ��1s,������nextLongId() 1����ֻ������1��
 * 2.��߾��ȣ�����accuracyLen=2 ��ʱ��ȷ��1/100 ��֮һ��,nextLongId()������1�����������100��
 * 3.���Ȳ��䣺�޸�incrLen=2     ��ʱ��ֻ��ȷ��1��,nextLongId()Ҳ������1�����������100��
 *   �ڸ߲��������,3��2��ͬ���ǣ�
 *      2��1�������ɵ�100��,ÿһ��������ƽ���ֲ���1���ڵ�
 *      3����1���ǰ��ʱ��(��������ʱ,����ٶ���ʱΪk )����100��,��101�������ʱ�Ῠסһ��ʱ��(1s - k)
 * 4.��߾��ȣ�accuracyLen=7 ��ʱ,nextLongId()ÿ��������� 10 000 000��id
 * 
 * ���Խ����ǣ�
 * ����Խ��(accuracyLen)�ڸ߲��������,������id�ڲ������ͻ�����Խ��,�����ٶȾ�Խ��
 * �ڲ�������ռ��λ��incrLen>0ʱ,�����ò�����������id�������� ǰ��ʱ�� ����
 * </pre>
 * 
 * <pre>
 * �ŵ㣺
 * ��Long�����������������������ݿ⣬����������ݱ��PrimaryKey���������ٶȿ죬����ֵ�ɶ�
 * ȱ�㣺
 * 1.�� PrimaryKey������ ��Ȼ������ʱ�䣬���������й����иı�ʱ�䲻��Ӱ��,���������ϵͳʱ������Ȼ�����������п������PrimaryKey�ظ���
 * 2.��ͬ��UUID,�������ڼ�Ⱥ
 * </pre>
 */
public class TimebasedIdGenerator {

    public static final long[] DECIMAL_SHIFT_BASE = new long[19];// long���19λ
    private static final Logger log = Log.getLogger();
    public static final int LONG_MAX_LEN = ("" + Long.MAX_VALUE).length(); // long���19λ
    private static final DecimalFormat percentageFormat = new DecimalFormat("#0.00%");
    /**
     * ��ʼ��ʱ�ĺ���������Ϊ��ʱ�����ϵͳʱ��ĸı���ı䣬 ���Լ��㷽��Ϊ��ʱ�����ͨ�� nanoTime ���������ʱ���
     */
    private static final Long startMilli;
    /**
     * ��ʼ��ʱ������������������ʱ��nanoTime��������ϵͳʱ����޸Ķ��ı�
     */
    private static final long startNano;

    static {
        // ��ʼ�� DECIMAL_SHIFT_BASE,Ҳ���ǰ�long�ͷ�Χ�ڵ����� 10��i�η���ǰ�������
        for (int i = 0; i < DECIMAL_SHIFT_BASE.length; i++) {
            DECIMAL_SHIFT_BASE[i] = (long) Math.pow(10, i);
        }

        // ��ʼ��startMilli,startNano
        // ������Ҫ�ر�ע��startMilli��Ϊ�ڳ�����ֻ��ȷ����,�����nanoTimeʱ,�ڳ�ʼ��ʱҪ��ͬ������
        // Ҳ���� startMilli% 1000 �����0,����ʹ��nanoTime��������� �������Ǹ�System.currentTimeMillis()һ�µ�
        long startMilliTmp = System.currentTimeMillis();
        long bias = startMilliTmp % 1000;
        if (bias > 0) {
            try {
                Thread.sleep(999 - bias);
            } catch (Exception e) {
            }
            int i = 0;
            while (true) {
                startMilliTmp = System.currentTimeMillis();
                if (startMilliTmp % 1000 == 0) {
                    break;
                }
                i++;
            }
        }
        startNano = System.nanoTime();
        startMilli = startMilliTmp;
    }

    /**
     * ʮ������λ
     * 
     * @param num ԭʼֵ
     * @param len len>0 Ч��ͬ�� << ����,Ҳ���Ǻ����len����,len<0 �ǳ��� 10��len�η�
     * @return ����ʱ����Long��Χ�ᱨIndexOutOfBoundsException
     */
    public static long decimalShift(long num, int len) {
        if (num == 0) {
            return 0;
        }
        if (len > 18) {
            throw new IndexOutOfBoundsException("decimalShift overflow,num:" + num + ",len:" + len);
        }
        if (len >= 0) {
            long r = num * DECIMAL_SHIFT_BASE[len];
            if (num > 0 ^ r > 0) {
                throw new IndexOutOfBoundsException("decimalShift overflow,num:" + num + ",len:" + len + "result:" + r);
            }
            return r;
        }
        return num / DECIMAL_SHIFT_BASE[len];
    }

    public static void main(String[] args) throws InterruptedException {
        // ���Է��� accuracyLen<=4ʱ,��ͻ�Ƚ϶�
        // TimebasedIdGenerator g = new TimebasedIdGenerator(4, false);// 1s�������1w��
        // // TimebasedIdGenerator g = new TimebasedIdGenerator(4, 0, false);
        //
        // long before = System.currentTimeMillis();
        // StringBuilder sb = new StringBuilder();
        // for (int i = 0; i < 20000; i++) {
        // sb.append(g.nextLongId()).append("\n");
        // }
        // log.error("span:{},{}", System.currentTimeMillis() - before, g);// accuracyLen=4ʱ,2w��Ҫ����s
        // System.out.println(sb);
        TimebasedIdGenerator tt = new TimebasedIdGenerator(4, 2, false);

        long before = System.currentTimeMillis();
        Map<String, Integer> map = new HashMap<String, Integer>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20000; i++) {
            String t = tt.nextId();
            Integer intt = map.get(t);
            if (intt != null) {
                System.out.println(t + "   -   " + intt + " - " + i);
                System.out.println(sb);
                System.exit(0);
            }
            map.put(t, i);
            sb.append(i).append("   ").append(t).append("\n");
        }
        log.error("span:{},{}", System.currentTimeMillis() - before, tt);// accuracyLen=4ʱ,2w��Ҫ����s
    }

    private long _accuracyDividend;
    private long _accuracyMod;
    private long _incrMod = 1;
    /**
     * ��ȷ������nλ
     */
    private int accuracyLen;
    /**
     * ������չʹ�õĳ���
     */
    private int canExpandLen = -1;
    /**
     * ����idʱ������ײ����
     */
    private long collides;
    /**
     * ����idʱ������ײ��,�����ܴ���
     */
    private long collideTries;
    private final DateStringUtil dsu;
    private final String pattern;
    /**
     * �ڲ�������
     */
    private long gens;
    private int incrLen;
    /**
     * ��¼��һ������ �� id����������ɵ�id���ϴ���ȣ�����Ҫ�ٴ�����
     */
    private volatile long lastId = -1;
    /**
     * �������������޶�ͬһʱ��ֻ��һ���߳̽������ɼ���
     */
    private final Lock LOCK = new ReentrantLock();
    private long sleepWhenCollide = 0;

    /**
     * <pre>
     * 
     * ��accuracyLen < 6�������,Ϊ�� ���� �߲�������ʱ,����������������,�� accuracyLen��incrLenλ,�� ������������,Ĭ��2λ
     * accuracyLen == 4 ,��  ������ȷ�� �ٷ�֮һ��,���2λ����
     * accuracyLen == 3 ,��  ������ȷ�� ʮ��֮һ��,���2λ����
     * accuracyLen == 2 ,��  ������ȷ�� ��,���2λ���� 
     * accuracyLen == 2 ,��ȷ��̫��  >_<,������Ҳ����Ϊ��
     * 
     * Ҳ����˵  1 < accuracyLen < 6ʱ,��Ĭ�Ͽ���incrLen = 2��������
     * </pre>
     * 
     * @param accuracyLen>0 ��ȷ������nλ(��ò�ҪС��3,�����ڸ߲������кܴ��ͻ)
     * @param fullYearMode true�� yyyyMMddHHmmss����ʾ ������ʱ����,falseʱ��yyMMddHHmmss��ʾ
     */
    public TimebasedIdGenerator(int accuracyLen, boolean fullYearMode) {
        this(accuracyLen, accuracyLen < 6 ? 2 : 0, fullYearMode);
    }

    /**
     * @param accuracyLen>0 ��ȷ������nλ(��ò�ҪС��3,�����ڸ߲������кܴ��ͻ)
     * @param incrLen>0 ָ���ڲ�ʹ�����������ɵ����ֳ���(�����2,��������100��ʱ,�����г�ͻ)
     * @param fullYearMode true�� yyyyMMddHHmmss����ʾ ������ʱ����,falseʱ��yyMMddHHmmss��ʾ
     */
    public TimebasedIdGenerator(int accuracyLen, int incrLen, boolean fullYearMode) {
        int accuracyLen_l = accuracyLen;
        int incrLen_l = incrLen;
        if (incrLen_l < 0) {
            incrLen_l = 0;
        }
        if (accuracyLen_l < 0) {
            accuracyLen_l = 0;
        }
        if (incrLen_l > accuracyLen_l) {
            incrLen_l = 0;
        }
        this.pattern = fullYearMode ? "yyyyMMddHHmmss" : "yyMMddHHmmss";
        this.dsu = DateStringUtil.getInstance(pattern);
        try {
            this.canExpandLen = LONG_MAX_LEN - pattern.length() - accuracyLen_l;
            this.accuracyLen = accuracyLen_l;
            this.incrLen = incrLen_l;

            this._accuracyDividend = DECIMAL_SHIFT_BASE[9 - (accuracyLen_l - incrLen_l)];
            this._accuracyMod = DECIMAL_SHIFT_BASE[accuracyLen_l - incrLen_l];

            if (_accuracyMod < 1000) {// ���ȶȱȽϵ�,�ڷ�����ײʱ,Ϊ�˽�ʡcpu,��������� sleep()һ��
                sleepWhenCollide = 1000 / _accuracyMod / 4; // ����ȥ������,��ʹ������,������ collide times������
                // 1000 / _accuracyMod / 10;
                // ���������� ���Ե��ķ�Χ�� 1~ 10
                // 1ʱsleep�,collide����,cpuռ�ý���
                // 10ʱsleep���,collide�϶�,cpuռ�ý϶�
                // �����sleep, cpuռ�����
            }
            this._incrMod = DECIMAL_SHIFT_BASE[incrLen_l];
        } catch (Exception e) {
        } finally {
            if (canExpandLen < 0) {
                log.error("CREATE ERROR:{}", this);
                throw new IllegalArgumentException(fullYearMode ? "fullYearMode's accuracyLen range is [0~5],but now accuracyLen:" + accuracyLen_l : "accuracyLen range is [0~7],but now accuracyLen:"
                        + this.accuracyLen);
            }
            log.debug("CREATE:{}", this);
        }
    }

    public long currentTimeMillis() {
        long pastNano = System.nanoTime() - startNano; // ����ʱ���
        long pastMilli = pastNano / 1000000; // ȡ�ú����
        return startMilli + pastMilli;
    }

    public int getAccuracyLen() {
        return accuracyLen;
    }

    public int getCanExpandLen() {
        return canExpandLen;
    }

    public long getCollides() {
        return collides;
    }

    public long getCollideTries() {
        return collideTries;
    }

    public long getGens() {
        return gens;
    }

    public int getIncrLen() {
        return incrLen;
    }

    public long getLastId() {
        return lastId;
    }

    public long getSleepWhenCollide() {
        return sleepWhenCollide;
    }

    public String nextId() {
        return nextLongId() + "";
    }

    public Date parse(String id) {
        int patternLen = pattern.length();
        if (id.length() < patternLen) {
            log.warn("id:{},pattern:{},length err", id, pattern);
            return new Date(0);// TODO:�����Ƿ��и��õķ�ʽ
        }
        String date = id.substring(0, patternLen);
        return dsu.parse(date);
    }

    public Date parse(long id) {
        return parse(id + "");
    }

    public long nextLongId() {
        LOCK.lock();
        // long incr = incrCounter.incrementAndGet();// ��������������,�ⲿ�Ѿ�����,���ﲻ�� acomicLong
        gens++;
        long incr = incrLen > 0 ? gens % _incrMod : 0; // �������һ������ʹ�õļ���

        boolean collide = incr == 0; // ��ʹ���������������,������һ�������Ƿ��г�ͻ
        int collideTimes = 0;
        long newId = 0;
        try {
            while (true) {
                long pastNano = System.nanoTime() - startNano; // ����ʱ���
                long pastMilli = pastNano / 1000000; // ȡ�ú����
                long pastAccuracyTime = (pastNano / _accuracyDividend) % _accuracyMod;
                Date now = new Date(startMilli + pastMilli);
                newId = Long.parseLong(dsu.format(now)) * _accuracyMod * _incrMod + pastAccuracyTime * _incrMod;
                // log.error("{},{},{},{}", new Object[] { Long.parseLong(dateFormat.format(now)), Long.parseLong(dateFormat.format(now)) * _accuracyMod * _incrMod, pastAccuracyTime,
                // pastAccuracyTime * _incrMod });
                // System.out.println(newId);
                if (incrLen > 0) {
                    if (collide && lastId == newId) {
                        sleepWhenCollide();
                        collideTimes++;
                        continue;
                    }
                    lastId = newId; // ���� lastPK
                    return newId + incr;
                }
                if (lastId != newId) {
                    lastId = newId; // ���� lastPK
                    return newId;
                }
                sleepWhenCollide();
                collideTimes++;
            }
            // do {
            // long pastNano = System.nanoTime() - startNano; // ����ʱ���
            // long pastMilli = pastNano / 1000000; // ȡ�ú����
            //
            // long pastAccuracyTime = (pastNano / _accuracyDividend) % _accuracyMod;
            // Date now = new Date(startMilli + pastMilli);
            // newId = Long.parseLong(dateFormat.format(now)) * _accuracyMod + pastAccuracyTime;
            //
            // if(incrLen > 0) {
            // long incr = incrCounter.incrementAndGet();
            //
            // }
            //
            // } while (lastId == newId); // ������ɵ���ͬ�����ٴμ���
            // lastId = newId; // ���� lastPK
        } finally {
            LOCK.unlock();
            if (collideTimes > 0) {
                collides++;
                collideTries += collideTimes;
                // log.info("gen id:{} encount collide,times:{}", new Object[] { newId + incr, collideTimes });
            }
        }
    }

    private void sleepWhenCollide() {
        try {
            Thread.sleep(sleepWhenCollide);// sleep(0) �����ڲ�Ӧ���д���0�����
        } catch (InterruptedException e) {
        }
    }

    /**
     * ���� ��ǰϵͳʱ�� �� {@link TimebasedIdGenerator}�ڲ�ά����ʱ��ƫ��
     * 
     * @return �������� �ڲ�ʱ���ܵ���/�Ͼ�,�������� �ڲ�ʱ��·�ÿ�/����(����������ܻ��� ���� �ظ�id��Σ��)
     */
    public long timeBias() {
        return System.currentTimeMillis() - currentTimeMillis();
    }

    @Override
    public String toString() {
        String statInfo = "";
        SimpleDateFormat sdf = (SimpleDateFormat) dsu.getDateFormat();
        if (gens > 0) {
            long collides_l = this.collides == 0 ? 1 : this.collides;
            statInfo = MessageFormat.format("[gens:{0},collide(tries/num):{2}/{1}->avg:{3},probability:{4},now:{5},bias:{6}] ", gens, collides_l, collideTries, collideTries / collides_l,
                    percentageFormat.format((double) this.collides / gens), sdf.format(currentTimeMillis()), timeBias());
        }
        return MessageFormat.format("TimebasedIdGenerator {0}[dateFormat={1}, accuracyLen={2}, incrLen={3}, canExpandLen={4}, sleepWhenCollide={5}]", statInfo, sdf.toPattern(), accuracyLen, incrLen,
                canExpandLen, sleepWhenCollide);
    }
}