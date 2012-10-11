package com.xunlei.util;

import java.util.Map;

/**
 * ������Map����ʱkey-value�Եļ������
 * 
 * @author ZengDong
 * @since 2010-11-24 ����06:46:38
 */
public class MapUtil {

    /**
     * ���key-value���Ƿ��Ӧ
     * 
     * @param keyvalue ���ɸ�key-value��
     */
    public static void checkKeyValueLength(Object... keyvalue) {
        if (keyvalue.length % 2 != 0) {
            throw new IllegalArgumentException("keyvalue.length is invalid:" + keyvalue.length);
        }
    }

    /**
     * �����ɸ�key-value�Է���ָ����map����
     * 
     * @param <K> key������
     * @param <V> value������
     * @param map Ҫ�����Map����
     * @param keyvalue ���ɸ�key-value��
     * @return map
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> buildMap(Map<K, V> map, Object... keyvalue) {
        checkKeyValueLength(keyvalue);
        for (int i = 0; i < keyvalue.length; i++) {
            map.put((K) keyvalue[i++], (V) keyvalue[i]);
        }
        return map;
    }
}
