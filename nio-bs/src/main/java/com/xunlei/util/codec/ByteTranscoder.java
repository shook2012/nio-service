package com.xunlei.util.codec;

import java.nio.ByteOrder;

/**
 * �ֽ�ת����
 * 
 * @author ZengDong
 * @since 2011-6-25 ����12:25:22
 */
public abstract class ByteTranscoder {

    /**
     * ���ģʽ�ֽ�ת����
     * 
     * @author ZengDong
     */
    public static class BigEndianTranscoder extends ByteTranscoder {

        private BigEndianTranscoder() {
        }

        /**
         * ��ö�ģʽ
         */
        @Override
        public ByteOrder getEndian() {
            return ByteOrder.BIG_ENDIAN;
        }

        /**
         * ��array�ֽ������д�index��ʼ��4λ������Int�͵��ĸ��ֽڵ���������ת��Ϊ10���Ƶ�����
         */
        @Override
        public int getInt(byte[] array, int index) {
            return (array[index] & 0xff) << 24 | (array[index + 1] & 0xff) << 16 | (array[index + 2] & 0xff) << 8 | (array[index + 3] & 0xff) << 0;
        }

        /**
         * ��array�ֽ������д�index��ʼ��8λ������Long�͵�8���ֽڵ���������ת��Ϊ10���Ƶ�����
         */
        @Override
        public long getLong(byte[] array, int index) {
            return ((long) array[index] & 0xff) << 56 | ((long) array[index + 1] & 0xff) << 48 | ((long) array[index + 2] & 0xff) << 40 | ((long) array[index + 3] & 0xff) << 32
                    | ((long) array[index + 4] & 0xff) << 24 | ((long) array[index + 5] & 0xff) << 16 | ((long) array[index + 6] & 0xff) << 8 | ((long) array[index + 7] & 0xff) << 0;
        }

        /**
         * ��array�ֽ������д�index��ʼ��2λ������Short�͵������ֽڵ���������ת��Ϊ10���Ƶ�����
         */
        @Override
        public short getShort(byte[] array, int index) {
            return (short) (array[index] << 8 | array[index + 1] & 0xFF);
        }

        /**
         * ��array�ֽ������д�index��ʼ��3λ�������޷���medium�͵�3���ֽڵ���������ת��Ϊ10���Ƶ�����
         */
        @Override
        public int getUnsignedMedium(byte[] array, int index) {
            return (array[index] & 0xff) << 16 | (array[index + 1] & 0xff) << 8 | (array[index + 2] & 0xff) << 0;
        }

        /**
         * ��array�д�index��ʼ��4��Ԫ������Ϊvalue��Ӧ��4���ֽڵ���
         */
        @Override
        public void setInt(byte[] array, int index, int value) {
            array[index] = (byte) (value >>> 24);
            array[index + 1] = (byte) (value >>> 16);
            array[index + 2] = (byte) (value >>> 8);
            array[index + 3] = (byte) (value >>> 0);
        }

        /**
         * ��array�д�index��ʼ��8��Ԫ������Ϊvalue��Ӧ��8���ֽڵ���
         */
        @Override
        public void setLong(byte[] array, int index, long value) {
            array[index] = (byte) (value >>> 56);
            array[index + 1] = (byte) (value >>> 48);
            array[index + 2] = (byte) (value >>> 40);
            array[index + 3] = (byte) (value >>> 32);
            array[index + 4] = (byte) (value >>> 24);
            array[index + 5] = (byte) (value >>> 16);
            array[index + 6] = (byte) (value >>> 8);
            array[index + 7] = (byte) (value >>> 0);
        }

        /**
         * ��array�д�index��ʼ��3��Ԫ������Ϊvalue��Ӧ��3���ֽڵ���
         */
        @Override
        public void setMedium(byte[] array, int index, int value) {
            array[index] = (byte) (value >>> 16);
            array[index + 1] = (byte) (value >>> 8);
            array[index + 2] = (byte) (value >>> 0);
        }

        /**
         * ��array�д�Index��ʼ��2��Ԫ������Ϊvalue��Ӧ��2���ֽڵ���
         */
        @Override
        public void setShort(byte[] array, int index, int value) {
            array[index] = (byte) (value >>> 8);
            array[index + 1] = (byte) (value >>> 0);
        }
    }

    /**
     * С��ģʽ�ֽ�ת����
     * 
     * @author ZengDong
     */
    public static class LittleEndianTranscoder extends ByteTranscoder {

        private LittleEndianTranscoder() {
        }

        /**
         * ��ö�ģʽ
         */
        @Override
        public ByteOrder getEndian() {
            return ByteOrder.LITTLE_ENDIAN;
        }

        /**
         * ��array�ֽ������д�index��ʼ��4λ������Int�͵��ĸ��ֽڵ���������ת��Ϊ10���Ƶ�����
         */
        @Override
        public int getInt(byte[] array, int index) {
            return (array[index] & 0xff) << 0 | (array[index + 1] & 0xff) << 8 | (array[index + 2] & 0xff) << 16 | (array[index + 3] & 0xff) << 24;
        }

        /**
         * ��array�ֽ������д�index��ʼ��8λ������Long�͵�8���ֽڵ���������ת��Ϊ10���Ƶ�����
         */
        @Override
        public long getLong(byte[] array, int index) {
            return ((long) array[index] & 0xff) << 0 | ((long) array[index + 1] & 0xff) << 8 | ((long) array[index + 2] & 0xff) << 16 | ((long) array[index + 3] & 0xff) << 24
                    | ((long) array[index + 4] & 0xff) << 32 | ((long) array[index + 5] & 0xff) << 40 | ((long) array[index + 6] & 0xff) << 48 | ((long) array[index + 7] & 0xff) << 56;
        }

        /**
         * ��array�ֽ������д�index��ʼ��2λ������Short�͵������ֽڵ���������ת��Ϊ10���Ƶ�����
         */
        @Override
        public short getShort(byte[] array, int index) {
            return (short) (array[index] & 0xFF | array[index + 1] << 8);
        }

        /**
         * ��array�ֽ������д�index��ʼ��3λ�������޷���medium�͵�3���ֽڵ���������ת��Ϊ10���Ƶ�����
         */
        @Override
        public int getUnsignedMedium(byte[] array, int index) {
            return (array[index] & 0xff) << 0 | (array[index + 1] & 0xff) << 8 | (array[index + 2] & 0xff) << 16;
        }

        /**
         * ��array�д�index��ʼ��4��Ԫ������Ϊvalue��Ӧ��4���ֽڵ���
         */
        @Override
        public void setInt(byte[] array, int index, int value) {
            array[index] = (byte) (value >>> 0);
            array[index + 1] = (byte) (value >>> 8);
            array[index + 2] = (byte) (value >>> 16);
            array[index + 3] = (byte) (value >>> 24);
        }

        /**
         * ��array�д�index��ʼ��8��Ԫ������Ϊvalue��Ӧ��8���ֽڵ���
         */
        @Override
        public void setLong(byte[] array, int index, long value) {
            array[index] = (byte) (value >>> 0);
            array[index + 1] = (byte) (value >>> 8);
            array[index + 2] = (byte) (value >>> 16);
            array[index + 3] = (byte) (value >>> 24);
            array[index + 4] = (byte) (value >>> 32);
            array[index + 5] = (byte) (value >>> 40);
            array[index + 6] = (byte) (value >>> 48);
            array[index + 7] = (byte) (value >>> 56);
        }

        /**
         * ��array�д�index��ʼ��3��Ԫ������Ϊvalue��Ӧ��3���ֽڵ���
         */
        @Override
        public void setMedium(byte[] array, int index, int value) {
            array[index] = (byte) (value >>> 0);
            array[index + 1] = (byte) (value >>> 8);
            array[index + 2] = (byte) (value >>> 16);
        }

        /**
         * ��array�д�Index��ʼ��2��Ԫ������Ϊvalue��Ӧ��2���ֽڵ���
         */
        @Override
        public void setShort(byte[] array, int index, int value) {
            array[index] = (byte) (value >>> 0);
            array[index + 1] = (byte) (value >>> 8);
        }
    }

    /**
     * ���ģʽ�Ķ�ת��������
     */
    public static final BigEndianTranscoder bigEndianTranscoder = new BigEndianTranscoder();
    /**
     * С��ģʽ�Ķ�ת��������
     */
    public static final LittleEndianTranscoder littleEndianTranscoder = new LittleEndianTranscoder();

    /**
     * ���ݶ�ģʽ�������Ӧ��ת����
     * 
     * @param endian
     * @return
     */
    public static ByteTranscoder getInstance(ByteOrder endian) {
        return endian == ByteOrder.BIG_ENDIAN ? bigEndianTranscoder : littleEndianTranscoder;
    }

    /**
     * ��array���鰴��char���ͽ���Ϊ����
     * 
     * @param array
     * @return
     */
    public int decodeChar(byte[] array) {
        return (char) getShort(array, 0);
    }

    /**
     * ��array���鰴��double���ͽ���Ϊ������
     * 
     * @param array
     * @return
     */
    public double decodeDouble(byte[] array) {
        return getDouble(array, 0);
    }

    /**
     * ��array���鰴��float���ͽ���Ϊ������
     * 
     * @param array
     * @return
     */
    public float decodeFloat(byte[] array) {
        return getFloat(array, 0);
    }

    /**
     * ��array���鰴��int���ͽ���Ϊint������
     * 
     * @param array
     * @return
     */
    public int decodeInt(byte[] array) {
        return getInt(array, 0);
    }

    /**
     * ��array���鰴��long���ͽ���Ϊlong������
     * 
     * @param array
     * @return
     */
    public long decodeLong(byte[] array) {
        return getLong(array, 0);
    }

    /**
     * ��array���鰴��Medium���ͽ���Ϊint������
     * 
     * @param array
     * @return
     */
    public int decodeMedium(byte[] array) {
        return getMedium(array, 0);
    }

    /**
     * ��array���鰴��short���ͽ���Ϊshort������
     * 
     * @param array
     * @return
     */
    public short decodeShort(byte[] array) {
        return getShort(array, 0);
    }

    /**
     * ��byteת��Ϊ�޷��ŵ�byte
     * 
     * @param b
     * @return
     */
    public short decodeUnsignedByte(byte b) {
        return (short) (b & 0xFF);
    }

    /**
     * ��array�������з��ŵ���������ת��Ϊlong
     * 
     * @param array
     * @return
     */
    public long decodeUnsignedInt(byte[] array) {
        return getUnsignedInt(array, 0);
    }

    /**
     * ��array�������з��ŵ���������ת��ΪMedium
     * 
     * @param array
     * @return
     */
    public int decodeUnsignedMedium(byte[] array) {
        return getUnsignedMedium(array, 0);
    }

    /**
     * ��array�������з��ŵ���������ת��Ϊshort
     * 
     * @param array
     * @return
     */
    public int decodeUnsignedShort(byte[] array) {
        return getUnsignedShort(array, 0);
    }

    /**
     * ��char���͵�valueת��Ϊ�ֽ�����
     * 
     * @param value
     * @return
     */
    public byte[] encodeChar(int value) {
        return encodeShort(value);
    }

    /**
     * ��double���͵�valueת��Ϊ�ֽ�����
     * 
     * @param value
     * @return
     */
    public byte[] encodeDouble(double value) {
        byte[] array = new byte[8];
        setDouble(array, 0, value);
        return array;
    }

    /**
     * ��float���͵�valueת��Ϊ�ֽ�����
     * 
     * @param value
     * @return
     */
    public byte[] encodeFloat(float value) {
        byte[] array = new byte[4];
        setFloat(array, 0, value);
        return array;
    }

    /**
     * ��int���͵�valueת��Ϊ�ֽ�����
     * 
     * @param value
     * @return
     */
    public byte[] encodeInt(int value) {
        byte[] array = new byte[4];
        setInt(array, 0, value);
        return array;
    }

    /**
     * ��long���͵�valueת��Ϊ�ֽ�����
     * 
     * @param value
     * @return
     */
    public byte[] encodeLong(long value) {
        byte[] array = new byte[8];
        setLong(array, 0, value);
        return array;
    }

    /**
     * ��Medium���͵�valueת��Ϊ�ֽ�����
     * 
     * @param value
     * @return
     */
    public byte[] encodeMedium(int value) {
        byte[] array = new byte[3];
        setMedium(array, 0, value);
        return array;
    }

    /**
     * ��short���͵�valueת��Ϊ�ֽ�����
     * 
     * @param value
     * @return
     */
    public byte[] encodeShort(int value) {
        byte[] array = new byte[2];
        setShort(array, 0, value);
        return array;
    }

    /**
     * ���array�����еĵ�indexλ
     * 
     * @param array
     * @param index
     * @return
     */
    public byte getByte(byte[] array, int index) {
        return array[index];
    }

    /**
     * ���array����ĵ�indexλ�ַ�
     * 
     * @param array
     * @param index
     * @return
     */
    public char getChar(byte[] array, int index) {
        return (char) getShort(array, index);
    }

    /**
     * ���array����ĵ�indexλ��
     * 
     * @param array
     * @param index
     * @return
     */
    public double getDouble(byte[] array, int index) {
        return Double.longBitsToDouble(getLong(array, index));
    }

    /**
     * ��ö�ģʽ
     * 
     * @return
     */
    public abstract ByteOrder getEndian();

    /**
     * ��õ�array�ĵ�indexλ����ת��Ϊfloat��
     * 
     * @param array
     * @param index
     * @return
     */
    public float getFloat(byte[] array, int index) {
        return Float.intBitsToFloat(getInt(array, index));
    }

    /**
     * ��array�ֽ������д�index��ʼ��4λ������Int�͵��ĸ��ֽڵ���������ת��Ϊ10���Ƶ�����
     * 
     * @param array
     * @param index
     * @return
     */
    public abstract int getInt(byte[] array, int index);

    /**
     * ��array�ֽ������д�index��ʼ��8λ������long�͵�8���ֽڵ���������ת��Ϊ10���Ƶ�����
     * 
     * @param array
     * @param index
     * @return
     */
    public abstract long getLong(byte[] array, int index);

    /**
     * ��array�ֽ������д�index��ʼ��3λ������Medium�͵�3���ֽڵ���������ת��Ϊ10���Ƶ�����
     * 
     * @param array
     * @param index
     * @return
     */
    public int getMedium(byte[] array, int index) {
        int value = getUnsignedMedium(array, index);
        if ((value & 0x800000) != 0) {
            value |= 0xff000000;
        }
        return value;
    }

    /**
     * ��array�ֽ������д�index��ʼ��2λ������short�͵�2���ֽڵ���������ת��Ϊ10���Ƶ�����
     * 
     * @param array
     * @param index
     * @return
     */
    public abstract short getShort(byte[] array, int index);

    /**
     * ��array�ֽ������д�index��ʼ��1λ������byte�͵�1���ֽڵ���������ת��Ϊ10���Ƶ�������
     * 
     * @param array
     * @param index
     * @return
     */
    public short getUnsignedByte(byte[] array, int index) {
        return (short) (getByte(array, index) & 0xFF);
    }

    /**
     * ��array�ֽ������д�index��ʼ��4λ������Int�͵�4���ֽڵ���������ת��Ϊ10���Ƶ�������
     * 
     * @param array
     * @param index
     * @return
     */
    public long getUnsignedInt(byte[] array, int index) {
        return getInt(array, index) & 0xFFFFFFFFL;
    }

    /**
     * ��array�ֽ������д�index��ʼ��3λ������Medium�͵�3���ֽڵ���������ת��Ϊ10���Ƶ�������
     * 
     * @param array
     * @param index
     * @return
     */
    public abstract int getUnsignedMedium(byte[] array, int index);

    /**
     * ��array�ֽ������д�index��ʼ��2λ������short�͵�2���ֽڵ���������ת��Ϊ10���Ƶ�������
     * 
     * @param array
     * @param index
     * @return
     */
    public int getUnsignedShort(byte[] array, int index) {
        return getShort(array, index) & 0xFFFF;
    }

    /**
     * ��array�д�index��ʼ��2��Ԫ������Ϊvalue��Ӧ��2���ֽڵ���
     * 
     * @param array
     * @param index
     * @param value
     */
    public void setChar(byte[] array, int index, int value) {
        setShort(array, index, value);
    }

    /**
     * ��array�д�index��ʼ��8��Ԫ������Ϊvalue��Ӧ��8���ֽڵ���
     * 
     * @param array
     * @param index
     * @param value
     */
    public void setDouble(byte[] array, int index, double value) {
        setLong(array, index, Double.doubleToRawLongBits(value));
    }

    /**
     * ��array�д�index��ʼ��4��Ԫ������Ϊvalue��Ӧ��4���ֽڵ���
     * 
     * @param array
     * @param index
     * @param value
     */
    public void setFloat(byte[] array, int index, float value) {
        setInt(array, index, Float.floatToRawIntBits(value));
    }

    /**
     * ��array�д�index��ʼ��4��Ԫ������Ϊvalue��Ӧ��4���ֽڵ���
     * 
     * @param array
     * @param index
     * @param value
     */
    public abstract void setInt(byte[] array, int index, int value);

    /**
     * ��array�д�index��ʼ��8��Ԫ������Ϊvalue��Ӧ��8���ֽڵ���
     * 
     * @param array
     * @param index
     * @param value
     */
    public abstract void setLong(byte[] array, int index, long value);

    /**
     * ��array�д�index��ʼ��3��Ԫ������Ϊvalue��Ӧ��3���ֽڵ���
     * 
     * @param array
     * @param index
     * @param value
     */
    public abstract void setMedium(byte[] array, int index, int value);

    /**
     * ��array�д�index��ʼ��2��Ԫ������Ϊvalue��Ӧ��2���ֽڵ���
     * 
     * @param array
     * @param index
     * @param value
     */
    public abstract void setShort(byte[] array, int index, int value);
}
