package com.hdd.common.util;

/**
 * 描述信息:
 * 
 * @author David
 * @version 1.0
 * 
 */
public class UInteger {
    private byte[] value = new byte[4];
    /**
     * A constant holding the minimum value a <code>byte</code> can have,0.
     */
    public static final long MIN_VALUE = 0L;

    /**
     * A constant holding the maximum value a <code>byte</code> can have,
     * 2<sup>32</sup>-1.
     */
    public static final long MAX_VALUE = 4294967295L;

    public UInteger(String s) {
        long temp = Long.parseLong(s);
        if (temp < MIN_VALUE || temp > MAX_VALUE) {
            throw new NumberFormatException("Value out of range. Value:\"" + s + "\"");
        }
        value[0] = (byte) (temp >> 24 & 0xff);
        value[1] = (byte) (temp >> 16 & 0xff);
        value[2] = (byte) (temp >> 8 & 0xff);
        value[3] = (byte) (temp >> 0 & 0xff);
    }

    public UInteger(byte b) {
        value[3] = b;
    }

    public UInteger(byte[] b) {
        for (int i = 0; i < b.length; i++) {
            value[i] = b[i];
        }
    }

    public UInteger(int i) {
        value[0] = (byte) (i >> 24 & 0xff);
        value[1] = (byte) (i >> 16 & 0xff);
        value[2] = (byte) (i >> 8 & 0xff);
        value[3] = (byte) (i >> 0 & 0xff);
    }

    public UInteger(long l) {
        if (l < MIN_VALUE || l > MAX_VALUE) {
            throw new NumberFormatException("Value out of range. Value:\"" + l + "\"");
        }
        value[0] = (byte) (l >> 24 & 0xff);
        value[1] = (byte) (l >> 16 & 0xff);
        value[2] = (byte) (l >> 8 & 0xff);
        value[3] = (byte) (l >> 0 & 0xff);
    }

    @Override
    public String toString() {
        long l = (value[0] & 0xffL) << 24 | (value[0] & 0xffL) << 16 | (value[0] & 0xffL) << 8 | (value[0] & 0xffL) << 0;
        return String.valueOf(l);
    }
}
