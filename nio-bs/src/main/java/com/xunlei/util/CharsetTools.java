package com.xunlei.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import org.apache.commons.lang.CharEncoding;

/**
 * 主要负责字符编码的转换
 * 
 * @author ZengDong
 * @since 2011-3-17 下午03:42:30
 */
public class CharsetTools {

    public static final Charset GB2312 = Charset.forName("GB2312");
    public static final Charset GBK = Charset.forName("GBK");
    public static final Charset GB18030 = Charset.forName("GB18030");
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset US_ASCII = Charset.forName("US-ASCII");
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public static final Charset UTF_16 = Charset.forName("UTF-16");
    public static final Charset UTF_16BE = Charset.forName("UTF-16BE");
    public static final Charset UTF_16LE = Charset.forName("UTF-16LE");

    /**
     * Constructs a new <code>String</code> by decoding the specified array of bytes using the given charset.
     * <p>
     * This method catches {@link UnsupportedEncodingException} and re-throws it as {@link IllegalStateException}, which should never happen for a required charset name. Use this method when the
     * encoding is required to be in the JRE.
     * </p>
     * 
     * @param bytes The bytes to be decoded into characters
     * @param charsetName The name of a required {@link java.nio.charset.Charset}
     * @return A new <code>String</code> decoded from the specified array of bytes using the given charset.
     * @throws IllegalStateException Thrown when a {@link UnsupportedEncodingException} is caught, which should never happen for a required charset name.
     * @see CharEncoding
     * @see String#String(byte[], String)
     */
    public static String newString(byte[] bytes, String charsetName) {
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw newIllegalStateException(charsetName, e);
        }
    }

    /**
     * Encodes the given string into a sequence of bytes using the named charset, storing the result into a new byte array.
     * <p>
     * This method catches {@link UnsupportedEncodingException} and rethrows it as {@link IllegalStateException}, which should never happen for a required charset name. Use this method when the
     * encoding is required to be in the JRE.
     * </p>
     * 
     * @param string the String to encode
     * @param charsetName The name of a required {@link java.nio.charset.Charset}
     * @return encoded bytes
     * @throws IllegalStateException Thrown when a {@link UnsupportedEncodingException} is caught, which should never happen for a required charset name.
     * @see CharEncoding
     * @see String#getBytes(String)
     */
    public static byte[] getBytes(String string, String charsetName) {
        if (string == null) {
            return null;
        }
        try {
            return string.getBytes(charsetName);
        } catch (UnsupportedEncodingException e) {
            throw newIllegalStateException(charsetName, e);
        }
    }

    /**
     * 返回一个IllegalStateException
     * 
     * @param charsetName
     * @param e
     * @return
     */
    private static IllegalStateException newIllegalStateException(String charsetName, UnsupportedEncodingException e) {
        return new IllegalStateException(charsetName + ": " + e);
    }

    private CharsetTools() {
    }
}
