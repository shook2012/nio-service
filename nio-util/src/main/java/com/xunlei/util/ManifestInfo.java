package com.xunlei.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipFile;
import org.slf4j.Logger;

/**
 * <pre>
 * http://stackoverflow.com/questions/3777055/reading-manifest-mf-file-from-jar-file-using-java
 * [reading MANIFEST.MF file from jar file using JAVA]
 * 
 * http://hxraid.iteye.com/blog/483115
 * 深入jar包：从jar包中读取资源文件
 * 
 * https://community.jboss.org/thread/157607?_sscc=t
 * [How to get manifest from war on JBoss AS]
 * 
 *  System.out.println("version : " + Hello.class.getPackage().getImplementationVersion() );
 *  
 *  
 *  [about package]
 *  http://docs.oracle.com/javase/7/docs/technotes/guides/versioning/spec/versioning2.html#wp89936
 *  http://docs.oracle.com/javase/tutorial/deployment/jar/packageman.html
 * 
 * @author 曾东
 * @since 2012-5-30 下午8:08:12
 */
public class ManifestInfo {

    private static Logger log = Log.getLogger();
    private static final String MANIFEST_NAME = JarFile.MANIFEST_NAME;

    private long time;
    private File jarFile;
    private Manifest manifest;
    private Map<?, ?> attributes;
    private URL url;

    public ManifestInfo(URL url, File jarFile, Manifest manifest, Map<?, ?> attributes, long time) {
        this.time = time;
        this.jarFile = jarFile;
        this.manifest = manifest;
        this.attributes = attributes;
        this.url = url;
    }

    public String getTimeString() {
        return DateStringUtil.DEFAULT.format(new Date(time));
    }

    public long getTime() {
        return time;
    }

    public File getJarFile() {
        return jarFile;
    }

    @Override
    public String toString() {
        String a = getTimeString() + "\t" + (jarFile == null ? "" : jarFile.getName());
        return a;
    }

    public Manifest getManifest() {
        return manifest;
    }

    public Map<?, ?> getAttributes() {
        return attributes;
    }

    public URL getUrl() {
        return url;
    }

    // time 跟 url 来判断是否相同
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (time ^ (time >>> 32));
        result = prime * result + ((url == null) ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ManifestInfo other = (ManifestInfo) obj;
        if (time != other.time)
            return false;
        if (url == null) {
            if (other.url != null)
                return false;
        } else if (!url.equals(other.url))
            return false;
        return true;
    }

    public static long getManifestInfoExpireMs = 10 * 60 * 1000; // 10分钟过期
    public static String coreJarStartWith = "xl-";
    private static long lastGetManifestInfoTime = 0;
    private static List<ManifestInfo> lastGetManifestInfo = null;
    private static long lastGetManifestInfoCoreTime = 0;
    private static List<ManifestInfo> lastGetManifestInfoCore = null;
    private static long lastGetManifestInfoOtherTime = 0;
    private static List<ManifestInfo> lastGetManifestInfoOther = null;

    public static List<ManifestInfo> getManifestInfoCore() {
        long now = System.currentTimeMillis();
        if (lastGetManifestInfoCore == null || now - lastGetManifestInfoCoreTime > getManifestInfoExpireMs) {
            synchronized (ManifestInfo.class) {
                lastGetManifestInfoCore = getManifestInfo(coreJarStartWith, true);
            }
        }
        return lastGetManifestInfoCore;
    }

    public static List<ManifestInfo> getManifestInfoOther() {
        long now = System.currentTimeMillis();
        if (lastGetManifestInfoOther == null || now - lastGetManifestInfoOtherTime > getManifestInfoExpireMs) {
            synchronized (ManifestInfo.class) {
                lastGetManifestInfoOther = getManifestInfo(coreJarStartWith, false);
            }
        }
        return lastGetManifestInfoOther;
    }

    public static List<ManifestInfo> getManifestInfo() {
        long now = System.currentTimeMillis();
        if (lastGetManifestInfo == null || now - lastGetManifestInfoTime > getManifestInfoExpireMs) {
            synchronized (ManifestInfo.class) {
                List<URL> urls = getManifestUrlList();
                List<ManifestInfo> list = new ArrayList<ManifestInfo>(urls.size());
                for (URL url : urls) {
                    long time = getManifestFileTime(url);
                    File jarFile = getManifestMappedJar(url);
                    Manifest manifest = getManifest(url);
                    Map<?, ?> attributes = getManifestAttributes(manifest);
                    list.add(new ManifestInfo(url, jarFile, manifest, attributes, time));
                }
                lastGetManifestInfo = list;
            }
        }
        return lastGetManifestInfo;
    }

    public static List<ManifestInfo> getManifestInfo(String jarFileStartWith, boolean include) {
        TreeMap<Long, ManifestInfo> m = new TreeMap<Long, ManifestInfo>();
        for (ManifestInfo mi : getManifestInfo()) {
            File jarFile = mi.jarFile;
            if (jarFile != null) {
                String jarName = jarFile.getName();
                if (jarName.startsWith(jarFileStartWith) ^ !include) {
                    m.put(-mi.time, mi);// 按时间倒序排
                }
            }
        }
        return new ArrayList<ManifestInfo>(m.values());
    }

    public static Manifest getManifest(URL url) {
        try {
            InputStream is = url.openStream();
            return new Manifest(is);
        } catch (IOException e) {
            log.error("", e);
            return null;
        }
    }

    public static Map<?, ?> getManifestAttributes(Manifest manifest) {
        Attributes mainAttribs = manifest.getMainAttributes();
        try {
            Field f = mainAttribs.getClass().getDeclaredField("map");
            f.setAccessible(true);
            Map<?, ?> m = (Map<?, ?>) f.get(mainAttribs);
            return m;
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    public static long getManifestFileTime(URL url) {
        Exception ex = null;
        String path = url.toString();
        String jarfile = "jar:file:";
        String file = "file:";
        if (path.startsWith(jarfile)) {
            int begin = jarfile.length();
            int end = ("!/" + MANIFEST_NAME).length();
            path = path.substring(begin, path.length() - end);
            try {
                ZipFile zf = new ZipFile(path);
                return zf.getEntry(MANIFEST_NAME).getTime();
            } catch (Exception e) {
                ex = e;
            }
        } else if (path.startsWith(file)) {
            int begin = file.length();
            path = path.substring(begin);
            File f = new File(path);
            if (f.exists()) {
                return f.lastModified();
            }
        }
        log.error("cant get lastModified,url:{}", url, ex);
        return -1;
    }

    public static File getManifestMappedJar(URL url) {
        Exception ex = null;
        String path = url.toString();
        String jarfile = "jar:file:";
        if (path.startsWith(jarfile)) {
            int begin = jarfile.length();
            int end = ("!/" + MANIFEST_NAME).length();
            path = path.substring(begin, path.length() - end);
            return new File(path);
        }
        return null;
    }

    // http://stackoverflow.com/questions/3777055/reading-manifest-mf-file-from-jar-file-using-java
    public static List<URL> getManifestUrlList() {
        try {
            Enumeration<URL> resEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
            List<URL> list = new ArrayList<URL>();
            while (resEnum.hasMoreElements()) {
                list.add(resEnum.nextElement());
            }
            return list;
        } catch (Exception e) {
            log.error("", e);
            return Collections.emptyList();
        }
    }

    public static void main(String[] args) {
        // System.err.println(getJarManifestInfo("xl-"));
        // System.err.println(getJarManifestInfo(""));
        // System.err.println(getManifestInfo("xl-", false));
        // System.err.println(getManifestInfo("xl-", true));
        System.err.println(getManifestInfoCore());
    }

}
