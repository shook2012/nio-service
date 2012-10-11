package com.xunlei.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * ������Ҫ�ṩ���IP��ַ�ķ���
 * 
 * @author ZengDong
 * @since 2010-5-6 ����09:42:56
 */
public class HttpUtil {

    private static Set<String> localIPSet;
    private static Set<String> localIPWith127001Set;
    private static String localSampleIP;

    /**
     * ���inetSocketAddress��Ӧ��IP��ַ
     * 
     * @param session
     * @return
     */
    public static String getIP(InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress != null) {
            InetAddress addr = inetSocketAddress.getAddress();
            if (addr != null) {
                return addr.getHostAddress();
            }
        }
        return "";
    }

    public static Set<String> getLocalIPWith127001() {
        if (localIPWith127001Set == null) {
            Set<String> localIPSetTmp = new LinkedHashSet<String>(3);
            try {
                Enumeration<?> e1 = NetworkInterface.getNetworkInterfaces();
                while (e1.hasMoreElements()) {
                    NetworkInterface ni = (NetworkInterface) e1.nextElement();

                    Enumeration<?> e2 = ni.getInetAddresses();
                    while (e2.hasMoreElements()) {
                        InetAddress ia = (InetAddress) e2.nextElement();
                        if (ia instanceof Inet6Address) {
                            continue;
                        }
                        String ip = ia.getHostAddress();
                        localIPSetTmp.add(ip);
                    }
                }
            } catch (SocketException e) {
                // log.error("", e);//��Ϊlogback�ڳ�ʼ��ʱ��Ҫ�õ��˷���,���Բ���ʹ��
                e.printStackTrace();
            }
            localIPWith127001Set = localIPSetTmp;
        }
        return localIPWith127001Set;
    }

    /**
     * ��ñ���IP����ȥ127.0.0.1֮���IP��
     */
    public static Set<String> getLocalIP() {
        if (localIPSet == null) {
            Set<String> localIPSetTmp = new LinkedHashSet<String>(3);
            localIPSetTmp.addAll(getLocalIPWith127001());
            localIPSetTmp.remove("127.0.0.1");
            localIPSet = localIPSetTmp;
        }
        return localIPSet;
    }

    /**
     * ��ñ�������IP
     * 
     * @return
     */
    public static String getLocalSampleIP() {
        if (localSampleIP == null) {
            Set<String> set = getLocalIP();
            localSampleIP = EmptyChecker.isEmpty(set) ? "N/A" : set.iterator().next();
        }
        return localSampleIP;
    }

    /**
     * ͨ��domainName���IP��ַ
     * 
     * @param domainName
     * @return
     */
    public static Set<String> getIPByDomainName(String domainName) {
        Set<String> domainIPSet = new LinkedHashSet<String>(2);
        try {
            InetAddress[] inets = InetAddress.getAllByName(domainName);
            for (InetAddress inetAddress : inets) {
                domainIPSet.add(inetAddress.getHostAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return domainIPSet;
    }
}
