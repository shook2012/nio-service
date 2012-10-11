package com.xunlei.netty.httpserver.cmd;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.xunlei.netty.httpserver.cmd.annotation.CmdCategory;
import com.xunlei.netty.httpserver.cmd.annotation.CmdDescr;
import com.xunlei.netty.httpserver.component.TimeoutInterrupter;
import com.xunlei.netty.httpserver.component.XLContextAttachment;
import com.xunlei.netty.httpserver.component.XLHttpRequest;
import com.xunlei.netty.httpserver.component.XLHttpResponse;
import com.xunlei.netty.httpserver.util.HttpServerConfig;
import com.xunlei.spring.BeanUtil;
import com.xunlei.spring.Config;
import com.xunlei.spring.SpringBootstrap;
import com.xunlei.util.EmptyChecker;
import com.xunlei.util.Log;
import com.xunlei.util.ResourceBundleUtil;
import com.xunlei.util.ValueUtil;
import com.xunlei.util.stat.TimeSpanStat;

/**
 * @author ZengDong
 * @since 2010-6-7 ����11:14:59
 */
@Component
public class CmdMappers {

    @Autowired
    private TimeoutInterrupter timeoutInterrupter;

    public static class StageTimeSpanStat extends TimeSpanStat {

        public StageTimeSpanStat(String name) {
            super(name, 1000, false, null);
            this.initFormat(40, 1);
        }

        @Override
        protected void warn(long end, long begin, Object arg) {
        }
    }

    public static class CmdMeta {

        private BaseCmd cmd;
        private Method method;
        private String name;
        protected StageTimeSpanStat stat;

        /**
         * ÿ��ҵ�����ʱ��,��λ��, <0 ��ʾ������ֱ��Disable,0��ʾ����ʱ,>0 ָ���峬ʱ����
         */
        private int timeout;

        private CmdMeta(BaseCmd cmd, Method method) {
            this.cmd = cmd;
            // if (method == null) {
            // try {
            // this.method = ReflectConvention.getDeclaredMethod(cmd.getClass(), defaultMethodName, XLHttpRequest.class, XLHttpResponse.class);
            // } catch (Exception e) {
            // throw new RuntimeException(e);
            // }
            // this.name = cmd.getClass().getSimpleName();
            // } else {
            this.method = method;
            this.name = cmd.getClass().getSimpleName() + "." + method.getName();
            // }
            stat = new StageTimeSpanStat(name);
        }

        public void access(XLContextAttachment attach) {
            stat.record(attach.getEncode(), attach.getProcess(), attach);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CmdMeta other = (CmdMeta) obj;
            if (cmd == null) {
                if (other.cmd != null)
                    return false;
            } else if (!cmd.equals(other.cmd))
                return false;
            if (method == null) {
                if (other.method != null)
                    return false;
            } else if (!method.equals(other.method))
                return false;
            return true;
        }

        public BaseCmd getCmd() {
            return cmd;
        }

        public Method getMethod() {
            return method;
        }

        public long getNum() {
            return stat.getAllNum();
        }

        public int getTimeout() {
            return timeout;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((cmd == null) ? 0 : cmd.hashCode());
            result = prime * result + ((method == null) ? 0 : method.hashCode());
            return result;
        }

        public boolean isDisable() {
            return timeout < 0;
        }

        /**
         * ���ü�����
         */
        public void resetCounter() {
            stat = new StageTimeSpanStat(name);
        }

        @Override
        public String toString() {
            return name;
        }

        public String[] getCategories() {
            CmdCategory c = method.getAnnotation(CmdCategory.class);
            if (c != null)
                return c.value();
            c = cmd.getClass().getAnnotation(CmdCategory.class);
            if (c != null)
                return c.value();
            String[] v = { cmd.getClass().getSimpleName() };
            return v;
        }

        /**
         * ��ȡCMD��ص�������Ϣ��������������DOC
         */
        public String getCmdDescription() {
            CmdDescr c = cmd.getClass().getAnnotation(CmdDescr.class);
            if (null != c) {
                return c.value();
            }
            return "";
        }

        public StageTimeSpanStat getStat() {
            return stat;
        }
    }

    private static final String GLOBAL_TIMEOUT = "GLOBAL_TIMEOUT";
    private static final Logger log = Log.getLogger();

    /**
     * ȥ������url��·�������һ��"/"��
     */
    private static String sanitizePath(String path) {
        int len = path.length();
        if (len > 1 && path.lastIndexOf('/') == len - 1) {
            path = path.substring(0, len - 1);
        }
        return path;
    }

    private Map<String, CmdMeta> annotation_cmd_map = Collections.emptyMap();
    private Map<String, CmdMeta> auto_cmd_map = Collections.emptyMap();
    private Map<String, CmdMeta> config_cmd_map = Collections.emptyMap();

    private Map<String, CmdMeta> cmdAllMap = new LinkedHashMap<String, CmdMeta>();
    private Map<CmdMeta, List<String>> reverseCmdAllSortedMap;

    @Config
    private String cmdmapper_config_filename = "cmdmapper";
    private ResourceBundle cmdMapperBundle = null;
    private final Map<CmdMeta, CmdMeta> cmdMetaUnite = new HashMap<CmdMeta, CmdMeta>();
    private List<BaseCmd> cmds;// ����spring�����ú��ҷ���cmd��淶�Ķ���
    @Autowired
    private HttpServerConfig config;

    private Set<Class<? extends BaseCmd>> disableCmdClass = new HashSet<Class<? extends BaseCmd>>(0);
    private Set<Method> disableCmdMethod = new HashSet<Method>(0);
    private PathMap fuzzyMap;
    private int globalTimeout = Integer.MIN_VALUE;

    private String _lower(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
    }

    // private String _upper(String name) {
    // return name.substring(0, 1).toUpperCase() + name.substring(1);
    // }

    private CmdMeta buildCmdMeta(String configStr) {
        String[] arg = configStr.split("\\.");
        if (arg.length >= 2) {// ���嵽����
            String cmdName = _lower(arg[0].trim());
            String cmdMethodName = arg[1].trim();
            try {
                BaseCmd cmd = BeanUtil.getTypedBean(cmdName);
                if (cmd == null)
                    return null;
                Method method = cmd.getClass().getMethod(cmdMethodName, XLHttpRequest.class, XLHttpResponse.class);
                return newCmdMeta(cmd, method);
            } catch (Exception e) {
                log.error("cann't find cmd:{},method:{}", new Object[] { cmdName, cmdMethodName, e });
            }
        } else {
            String cmdName = _lower(configStr);
            try {
                BaseCmd cmd = BeanUtil.getTypedBean(cmdName);
                if (cmd == null)
                    return null;
                // return newCmdMeta(cmd, null);
                Method method = cmd.getClass().getMethod(config.getCmdDefaultMethod(), XLHttpRequest.class, XLHttpResponse.class);
                return newCmdMeta(cmd, method);
            } catch (Exception e) {
                log.error("cann't find cmd:{}", new Object[] { cmdName, e });
            }
        }
        return null;
    }

    private BaseCmd getCmd(String name) {
        Object obj = BeanUtil.getTypedBean(name);
        if (obj instanceof BaseCmd) {
            Class<?> clazz = obj.getClass();
            if (clazz.getAnnotation(Deprecated.class) != null || disableCmdClass.contains(clazz))// Deprecated/Disable��cmd������
                return null;
            return (BaseCmd) obj;
        }
        return null;
    }

    public Map<CmdMeta, List<String>> getReverseCmdAllSortedMap() {
        if (reverseCmdAllSortedMap == null) {
            Map<CmdMeta, List<String>> tmp = new LinkedHashMap<CmdMeta, List<String>>();
            _buildReverseCmdAllSortedMap(tmp, auto_cmd_map);
            _buildReverseCmdAllSortedMap(tmp, annotation_cmd_map);
            _buildReverseCmdAllSortedMap(tmp, config_cmd_map);
            reverseCmdAllSortedMap = tmp;
        }
        return reverseCmdAllSortedMap;
    }

    private void _buildReverseCmdAllSortedMap(Map<CmdMeta, List<String>> tmp, Map<String, CmdMeta> ori) {
        for (Entry<String, CmdMeta> e : ori.entrySet()) {
            CmdMeta meta = e.getValue();
            String url = e.getKey();
            List<String> list = tmp.get(meta);
            if (list == null) {
                list = new ArrayList<String>(1);
                tmp.put(meta, list);
            }
            list.add(url);
        }
    }

    public ResourceBundle getCmdMapperBundle() {
        if (cmdMapperBundle == null) {
            cmdMapperBundle = ResourceBundleUtil.reload(cmdmapper_config_filename);
            if (cmdMapperBundle == null) {
                cmdMapperBundle = new ResourceBundle() {

                    @Override
                    public Enumeration<String> getKeys() {
                        return new Enumeration<String>() {

                            @Override
                            public boolean hasMoreElements() {
                                return false;
                            }

                            @Override
                            public String nextElement() {
                                return null;
                            }
                        };
                    }

                    @Override
                    protected Object handleGetObject(String key) {
                        return null;
                    }
                };
            }
        }
        return cmdMapperBundle;
    }

    public CmdMeta getCmdMeta(String path) throws Exception {
        path = sanitizePath(path);
        CmdMeta meta = cmdAllMap.get(path);
        if (meta == null && fuzzyMap != null)
            return (CmdMeta) fuzzyMap.match(path);
        return meta;
    }

    private String getCmdName(BaseCmd cmd) {
        String cmdSuffix = config.getCmdSuffix();
        String cmdName = _lower(cmd.getClass().getSimpleName());

        // ������ ��Ϊcglib������������滻�����
        // 2011-03-18 ԭ��û�д�bug��ԭ������Ϊԭ���ǲ���ͨ��getCmds() �����BaseCmd�ټ��������className,����ֱ��ͨ�� Bootstrap.CONTEXT.getBeanDefinitionNames()���ҵ���
        int idx = cmdName.indexOf('$');
        if (idx > 0) {
            cmdName = cmdName.substring(0, idx);
        }

        if (cmdName.endsWith(cmdSuffix)) {
            cmdName = cmdName.substring(0, cmdName.length() - cmdSuffix.length());
        }
        return cmdName;
    }

    private List<BaseCmd> getCmds() {
        if (cmds == null) {
            cmds = new ArrayList<BaseCmd>();
            for (String name : SpringBootstrap.getContext().getBeanDefinitionNames()) {
                BaseCmd cmd = getCmd(name);
                if (cmd != null) {
                    cmds.add(cmd);
                }
            }
        }
        return cmds;
    }

    /**
     * ȫ�����ʱ����,��λ��
     */
    public int getGlobalTimeout() {
        if (globalTimeout == Integer.MIN_VALUE) {
            int noTimeout = 0;
            try {
                String v = getCmdMapperBundle().getString(GLOBAL_TIMEOUT);
                globalTimeout = ValueUtil.getInteger(v, noTimeout);// Ĭ��Ϊ0,Ҳ���ǲ���ʱ
            } catch (Exception e) {
                globalTimeout = noTimeout;
            }
        }
        return globalTimeout;
    }

    /**
     * ���һ�������Ӧ�ĳ�ʱ����λ��
     */
    public int getCmdMetaTimeout(CmdMeta cmd) {
        int defaultTimeout = getGlobalTimeout();
        try {
            String v = getCmdMapperBundle().getString(cmd.toString());
            return ValueUtil.getInteger(v, defaultTimeout);
        } catch (Exception e) {
            return defaultTimeout;
        }
    }

    public Map<String, CmdMeta> initCmdMapperDefinedMap() {
        Map<String, CmdMeta> tmp = new LinkedHashMap<String, CmdMeta>();
        // boolean cmdMiscMethodExtenable = config.isCmdMiscMethodExtenable();// ���Զ�����AUTO_CMD_MAP,�Ƿ�չ������cmdClass(���ܼ̳и��෽��,������ʾ�ص���/process����)
        // for (BaseCmd cmd : getCmds()) {
        // Class<?> clazz = cmd.getClass();
        // // 1.����class����ӳ��
        // CmdMapper m = clazz.getAnnotation(CmdMapper.class);
        // if (m != null) {
        // for (String url : m.value()) {
        // putUrl(tmp, url, newCmdMeta(cmd, null));
        // }
        // }
        // // 2.���ط�������ӳ��
        // // 2011-03-17 ֱ����autoMap�������ɵ�cmdMetaUnite����ʼ��
        //
        // // Method[] mehods = cmdMiscMethodExtenable ? clazz.getMethods() : clazz.getDeclaredMethods();
        // // for (Method method : mehods) {
        // // if (!isCmdMethod(cmd, method))
        // // continue;
        // // CmdMeta meta = newCmdMeta(cmd, method);
        // // if ((m = method.getAnnotation(CmdMapper.class)) == null)
        // // continue;
        // // for (String url : m.value()) {
        // // putUrl(tmp, url, meta);
        // // }
        // // }
        // }
        // 2.���ط�������ӳ��
        for (CmdMeta meta : cmdMetaUnite.values()) {
            CmdMapper m = meta.getMethod().getAnnotation(CmdMapper.class);
            if (m != null) {
                for (String url : m.value()) {
                    putUrl(tmp, url, meta);
                }
            }
        }
        annotation_cmd_map = tmp;
        log.error("ANNOTATION_MAP:\t\t{}", annotation_cmd_map);
        cmdAllMap.putAll(tmp);
        return tmp;
    }

    public Map<String, CmdMeta> initAutoMap() {
        Map<String, CmdMeta> tmp_auto = new LinkedHashMap<String, CmdMeta>();

        String cmdDefaultMethod = config.getCmdDefaultMethod();
        for (BaseCmd cmd : getCmds()) {
            Class<?> clazz = cmd.getClass();
            ArrayList<String> cmdNameList = new ArrayList<String>(1);
            CmdPath cmdPathForClazz = clazz.getAnnotation(CmdPath.class);
            if (clazz.getAnnotation(CmdOverride.class) == null) {
                cmdNameList.add(getCmdName(cmd));
            }
            if (cmdPathForClazz != null) {
                for (String alias : cmdPathForClazz.value()) {
                    cmdNameList.add(alias);
                }
            }

            for (String cmdName : cmdNameList) {
                // Method[] mehods = cmdMiscMethodExtenable ? clazz.getMethods() : clazz.getDeclaredMethods();
                Method[] mehods = clazz.getDeclaredMethods();
                for (Method method : mehods) {
                    if (!isCmdMethod(cmd, method))
                        continue;
                    CmdMeta meta = null;
                    String methodName = method.getName();

                    // if (method.getDeclaringClass().equals(cmd.getClass())) {
                    // if (methodName.equals(cmdDefaultMethod)) {
                    // meta = newCmdMeta(cmd, method);
                    //
                    // tmp_auto.put("/" + cmdName, meta);
                    // if (cmdDefaultMethodVisiable)
                    // tmp_expand.put(methodUrl, meta);
                    // } else {
                    ArrayList<String> urlList = new ArrayList<String>(1);
                    if (method.getAnnotation(CmdOverride.class) == null) {
                        if (methodName.equals(cmdDefaultMethod)) {
                            urlList.add(MessageFormat.format("/{0}", cmdName));
                        } else {
                            urlList.add(MessageFormat.format("/{0}/{1}", cmdName, methodName));
                        }
                    }
                    CmdPath cmdPathForMethod = method.getAnnotation(CmdPath.class);
                    if (cmdPathForMethod != null) {
                        for (String alias : cmdPathForMethod.value()) {
                            urlList.add(MessageFormat.format("/{0}/{1}", cmdName, alias));
                        }
                    }

                    meta = newCmdMeta(cmd, method);
                    for (String url : urlList) {
                        tmp_auto.put(url, meta);
                    }

                    // }
                    // } else {// ˵���Ǹ���� ���÷���
                    // if (cmdMiscMethodExtenable) {
                    // if (methodName.equals(cmdDefaultMethod)) {
                    // meta = newCmdMeta(cmd, method);
                    // tmp_expand.put("/" + cmdName, meta);
                    // if (cmdDefaultMethodVisiable)
                    // tmp_expand.put(methodUrl, meta);
                    // } else {
                    // meta = newCmdMeta(cmd, method);
                    // tmp_expand.put(methodUrl, meta);
                    // }
                    // }
                    // }
                }
            }
        }
        auto_cmd_map = tmp_auto;
        log.error("AUTO_MAP:\t\t{}", auto_cmd_map);
        cmdAllMap.putAll(tmp_auto);

        Map<String, CmdMeta> tmp = new LinkedHashMap<String, CmdMeta>(tmp_auto.size());
        tmp.putAll(tmp_auto);
        return tmp;
    }

    public Map<String, CmdMeta> initConfigMap() {
        ResourceBundle bundle = getCmdMapperBundle();
        Map<String, CmdMeta> tmp = new LinkedHashMap<String, CmdMeta>();
        // 3.����cmdmapper����ӳ��
        if (bundle != null) {
            for (String url : bundle.keySet()) {
                if (!isUrlConfig(url)) {
                    continue;
                }
                String value = bundle.getString(url).trim();
                CmdMeta cm = buildCmdMeta(value);
                if (cm != null)
                    putUrl(tmp, url, cm);
            }
        }
        config_cmd_map = tmp;
        log.error("CONFIG_MAP:\t\t{}", config_cmd_map);
        cmdAllMap.putAll(tmp);

        return tmp;
    }

    private boolean isCmdMethod(BaseCmd cmd, Method method) {
        if (method.getAnnotation(Deprecated.class) != null || disableCmdMethod.contains(method))
            return false;
        if (!Modifier.isPublic(method.getModifiers()))
            return false;
        Class<?>[] pts = method.getParameterTypes();
        if (pts.length != 2)
            return false;
        if (!pts[0].isAssignableFrom(XLHttpRequest.class) || !pts[1].isAssignableFrom(XLHttpResponse.class))
            return false;
        return true;
    }

    private boolean isUrlConfig(String cmdMapperBundleKey) {
        return cmdMapperBundleKey.startsWith("*") || cmdMapperBundleKey.startsWith("/");
    }

    /**
     * <pre>
     * 2011-01-25 Ϊ�˽��ͳ��cmd���Ե�����������,�ڴ�����ҪͳһCmdMeta
     * �������cmd,methodһ��,�ڴ���ֻ������ͬ������
     */
    private CmdMeta newCmdMeta(BaseCmd cmd, Method method) {
        CmdMeta tmp = new CmdMeta(cmd, method);
        CmdMeta ori = cmdMetaUnite.get(tmp);
        if (ori == null) {
            cmdMetaUnite.put(tmp, tmp);
            return tmp;
        }
        return ori;
    }

    public void printFuzzyMap() {
        if (fuzzyMap != null)
            log.error("FUZZY_MAP:\t\t{}", fuzzyMap);
    }

    private void putUrl(Map<String, CmdMeta> tmp, String url, CmdMeta meta) {
        if (url == null)
            return;
        url = url.trim();
        if (url.isEmpty())
            return;
        if (url.contains("*")) {// ģ��ƥ��
            if (fuzzyMap == null)
                fuzzyMap = new PathMap();
            fuzzyMap.put(url, meta);
            return;
        }
        // if (!url.startsWith("/")) {
        // url = "/" + url;
        // // log.info("ignore url:{} for CmdMappers", url);
        // // return;
        // }
        url = sanitizePath(url);
        tmp.put(url, meta);
    }

    /**
     * <pre>
     * ������������ļ�����
     * ע��,����û�н��в�������
     * Ҳ���������ù�����,HttpServer�����������������
     * Ӱ�첻��,���Բ��Դ�������д���
     */
    public void resetAllCounter() {
        for (CmdMeta meta : cmdMetaUnite.keySet()) {
            meta.resetCounter();
        }
    }

    @Config(resetable = true)
    private boolean interruptClosedChannel = true;// 2011-12-19 ����Ϊ��֧��attachCmd����Ҫ�κ�ʱ�򶼴�

    public StringBuilder resetCmdConfig() {
        cmdMapperBundle = null;// ���ԭ����cmdMapperBundle,�����������¶������ļ�
        globalTimeout = Integer.MIN_VALUE;// ����
        boolean damonScannerEnable = interruptClosedChannel;

        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        for (CmdMeta meta : cmdMetaUnite.keySet()) {
            meta.timeout = getCmdMetaTimeout(meta);
            if (meta.isDisable()) {
                sb.append(meta.name).append(", ");
            } else if (meta.getTimeout() > 0) {// ֻҪ�и����������ó�ʱ,��̨ɨ������Ҫ��
                damonScannerEnable = true;
                sb1.append(meta.name).append("=").append(meta.getTimeout()).append(", ");
            }
        }
        StringBuilder r = new StringBuilder();

        if (EmptyChecker.isNotEmpty(sb)) {
            String msg = sb.substring(0, sb.length() - 2);
            log.error("DISABLE_SET:\t\t{{}}", msg);
            r.append("DISABLE_SET:\t\t{").append(msg).append("}\n");
        }
        if (EmptyChecker.isNotEmpty(sb1)) {
            String msg = sb1.substring(0, sb1.length() - 2);
            log.error("TIMEOUT_SET:\t\t{{}}", msg);
            r.append("TIMEOUT_SET:\t\t{").append(msg).append("}\n");
        }
        timeoutInterrupter.setThreadInterrupterEnable(damonScannerEnable);
        boolean enable = timeoutInterrupter.isEnable();

        r.append(enable ? "TimeoutInterrupter Enable" : "TimeoutInterrupter Disable");
        return r;
    }
}
