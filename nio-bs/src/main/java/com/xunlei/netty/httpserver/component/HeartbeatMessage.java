package com.xunlei.netty.httpserver.component;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import com.xunlei.util.DateStringUtil;
import com.xunlei.util.EmptyChecker;
import com.xunlei.util.SystemInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HeartbeatMessage {

    private static final List<String> default_logic_urls = Arrays.asList(new String[] { "/" });
    /** ���������� */
    private String name;
    /** ������������IP */
    private List<String> ip;
    /** ������������hostName */
    private String hostName;
    /** ����PID */
    private int pid;
    /** ruiz�������http�˿ں� */
    private int port;
    /** ruiz��Ҫ��ص�url�б���/��ͷ����/echo */
    private List<String> logicUrls;
    /** �����ϱ�������ĵ����������������reportMsg���������heartbeatSec��˵���˳���û�������� */
    private int heartbeatSec;
    /** ��������ʱ�� */
    private long startupTime;
    /** �������ı�ǩ�����ڼ�����Ľ��з��������ʾ */
    private List<String> tags;
    @JsonIgnore
    private List<String> logicAbsoluteUrls;
    @JsonIgnore
    private String remoteIp; // ������Ļص��ã��� Զ��ip��¼����
    @JsonIgnore
    private String startupTimeStr;
    @JsonIgnore
    private long reportTime = System.currentTimeMillis(); // ����ֶ��� ��������յ������õ�
    @JsonIgnore
    public String globalId;

    public HeartbeatMessage() {
    }

    public HeartbeatMessage(int port, List<String> logicUrls, List<String> ip, int heartbeatSec, List<String> tags) {
        this.port = port;
        this.ip = ip;
        this.logicUrls = logicUrls;
        this.heartbeatSec = heartbeatSec;

        this.name = SystemInfo.COMMAND_FULL;
        this.hostName = SystemInfo.HOSTNAME;
        this.pid = SystemInfo.PID;
        this.startupTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        this.tags = tags;
    }

    public String getGlobalId() {
        if (this.globalId == null) {
            this.globalId = (getNameExcludeArgs() + ":" + this.ip + ":" + this.port);
        }
        return this.globalId;
    }

    public String getNameExcludeArgs() {
        return SystemInfo.getCommand(this.name, false);
    }

    public int getHeartbeatSec() {
        return this.heartbeatSec;
    }

    public List<String> getIp() {
        return this.ip;
    }

    public List<String> getLogicAbsoluteUrls() {
        if (this.logicAbsoluteUrls == null) {
            this.logicAbsoluteUrls = new ArrayList<String>();
            String mainUrl = "http://" + getRemoteIp() + ":" + this.port;
            for (String u : getLogicUrls()) {
                this.logicAbsoluteUrls.add(mainUrl + u);
            }
        }
        return this.logicAbsoluteUrls;
    }

    public List<String> getLogicUrls() {
        if (EmptyChecker.isEmpty(this.logicUrls)) {
            this.logicUrls = default_logic_urls;
        }
        return this.logicUrls;
    }

    public String getName() {
        return this.name;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPid() {
        return this.pid;
    }

    public int getPort() {
        return this.port;
    }

    @JsonIgnore
    public long getRemainTime() {
        return this.reportTime + this.heartbeatSec * 1000 - System.currentTimeMillis();
    }

    public String getRemoteIp() {
        return this.remoteIp;
    }

    public long getReportTime() {
        return this.reportTime;
    }

    public long getStartupTime() {
        return this.startupTime;
    }

    public String getStartupTimeStr() {
        if (this.startupTimeStr == null) {
            this.startupTimeStr = DateStringUtil.DEFAULT.format(new Date(this.startupTime));
        }
        return this.startupTimeStr;
    }

    @JsonIgnore
    public boolean isWarn() {
        return getRemainTime() < 0L;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public List<String> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        return String.format("ReportMessage [name=%s, hostName=%s, ip=%s, pid=%s, port=%s, logicUrls=%s, heartbeatSec=%s, startupTime=%s, startupTimeStr=%s, reportTime=%s]", name, hostName, ip, pid,
                port, logicUrls, heartbeatSec, startupTime, startupTimeStr, reportTime);
    }

    // public String toString() {
    // return String.format(
    // "ReportMessage [name=%s, ip=%s, pid=%s, port=%s, logicUrls=%s, startupTimeStr=%s, reportTime=%s, heartbeatSec=%s]",
    // new Object[] { this.name, this.ip, Integer.valueOf(this.pid), Integer.valueOf(this.port), this.logicUrls, getStartupTimeStr(), Long.valueOf(this.reportTime),
    // Integer.valueOf(this.heartbeatSec) });
    // }
}