package com.xunlei.netty.httpserver.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xunlei.json.JSONUtil;
import com.xunlei.netty.httpserver.util.HttpServerConfig;
//import com.xunlei.proxy.HttpClientUtil;
import com.xunlei.spring.AfterConfig;
import com.xunlei.spring.Config;
import com.xunlei.util.HttpUtil;
import com.xunlei.util.InetAddressCacheUtil;
import com.xunlei.util.Log;
import com.xunlei.util.StringTools;
import com.xunlei.util.concurrent.ConcurrentUtil;

@Service
public class HeartbeatClient {

    private HeartbeatMessage message;
    private ScheduledFuture<?> scheduledFuture;
    @Autowired
    private HttpServerConfig httpServerConfig;
    @Config(resetable = true)
    // private List<String> monitorUrls = Arrays.asList(new String[] { "/stat", "/echo" });
    private List<String> heartbeatMonitorUrls = Collections.emptyList();
    @Config(resetable = true)
    private int heartbeatSec = 5;
    @Config(resetable = true)
    private int heartbeatTestSec = 10;
    @Config(resetable = true)
    private String heartbeatUrl = "http://armero.cc.sandai.net:2012/heartbeat/login";
    @Config(resetable = true)
    private List<String> heartbeatIgnoreIpList = Arrays.asList(new String[] { "192.168." }); // 支持写成 127.0. 因为内部是 contains方式
    @Config(resetable = true)
    private List<String> heartbeatTags = Arrays.asList(new String[] { "N/A" });
    private Logger log = Log.getLogger();

    /**
     * 想关闭监控上报，可以把本地ip放到 heartbeatIgnoreIpList，或者把 heartbeatSec设置成<=0,或者把 heartbeatUrl设置为空
     */
    @AfterConfig
    public void init() {
        this.message = null; // 重新配置时，把 message重新配置

        if (this.scheduledFuture != null) {
            this.scheduledFuture.cancel(true);
        }
        if (this.heartbeatTestSec <= this.heartbeatSec) {
            throw new RuntimeException("heartbeatTestSec(" + this.heartbeatTestSec + ")<=heartbeatSec(" + this.heartbeatSec + ")");
        }
        String ignoreIp = getIgnoreIp();
        if (StringTools.isEmpty(heartbeatUrl) || InetAddressCacheUtil.isUnknownHostByUrl(heartbeatUrl)) {
            this.log.warn("HeartbeatClient         OFF,heartbeatUrl[{}] isUnknownHost", heartbeatUrl);
        } else if (StringTools.isNotEmpty(ignoreIp)) {
            this.log.warn("HeartbeatClient         OFF,localip[{}] is ignore", ignoreIp);
        } else if (this.heartbeatSec <= 0) {
            this.log.warn("HeartbeatClient         OFF,heartbeatSec:{} is negative", Integer.valueOf(this.heartbeatSec));
        } else {
            this.log.warn("HeartbeatClient         ON,heartbeatSec:{},heartbeatUrl:{},heartbeatTestSec:{}",
                    new Object[] { Integer.valueOf(this.heartbeatSec), this.heartbeatUrl, Integer.valueOf(this.heartbeatTestSec) });
            this.scheduledFuture = ConcurrentUtil.getDaemonExecutor().scheduleWithFixedDelay(new Runnable() {

                public void run() {
                    HeartbeatClient.this.heartbeat();
                }
            }, 0L, this.heartbeatSec, TimeUnit.SECONDS);
        }
    }

    public String getIgnoreIp() {
        Set<String> ip = HttpUtil.getLocalIP();
        for (String i : ip) {
            for (String ignoreIpSecction : this.heartbeatIgnoreIpList) {
                if (i.contains(ignoreIpSecction)) {
                    return i;
                }
            }
        }
        return null;
    }

    public HeartbeatMessage getMessage() {
        if (this.message == null) {
            this.message = new HeartbeatMessage(this.httpServerConfig.getListen_port(), this.heartbeatMonitorUrls, new ArrayList<String>(HttpUtil.getLocalIP()), this.heartbeatTestSec,
                    this.heartbeatTags);
        }
        return this.message;
    }

    public void heartbeat() {
        HeartbeatMessage msg = getMessage();
        String postContent = "info=" + JSONUtil.fromObject(msg);
//        HttpClientUtil.httpPost(this.heartbeatUrl, postContent);
    }
}