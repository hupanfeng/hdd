package com.hdd.common.util;

/**
 * 描述信息:
 * 
 * @author David
 * @version 1.0
 * 
 */
public class UByte {
    private byte[] value = new byte[1];
    /**
     * A constant holding the minimum value a <code>byte</code> can have,0.
     */
    public static final int MIN_VALUE = 0;

    /**
     * A constant holding the maximum value a <code>byte</code> can have,
     * 2<sup>8</sup>-1.
     */
    public static final int MAX_VALUE = 255;

    public UByte(String b) {
        int temp = Integer.parseInt(b);
        if (temp < MIN_VALUE || temp > MAX_VALUE) {
            throw new NumberFormatException("Value out of range. Value:\"" + b + "\"");
        }
        value[0] = (byte) (temp & 0xff);
    }

    public UByte(byte b) {
        value[0] = b;
    }

    public UByte(int b) {
        if (b < MIN_VALUE || b > MAX_VALUE) {
            throw new NumberFormatException("Value out of range. Value:\"" + b + "\"");
        }
        value[0] = (byte) (b & 0xff);
    }

    @Override
    public String toString() {
        short temp = (short) (value[0] & 0xff);
        return String.valueOf(temp);
    }
}
