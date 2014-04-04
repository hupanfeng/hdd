package com.hdd.common.util;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * 字节流工具类
 * 
 * @author david
 * @version 1.0
 */
public class ByteUtil {
    private static final char[] hex = "0123456789ABCDEF".toCharArray();
    private static String encode = "GBK";

    public static String asciiToString(String asc) {
        if (asc == null || asc.length() == 0 || asc.length() % 2 != 0) {
            return "";
        }
        ByteBuffer ascBuffer = ByteBuffer.allocate(asc.length() / 2);

        for (int i = 0; i < asc.length(); i = i + 2) {
            Integer asciiValue = Integer.parseInt(asc.substring(i, i + 2), 16);
            ascBuffer.put(asciiValue.byteValue());
        }
        String temp = "";
        try {
            temp = new String(ascBuffer.array(), encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return temp;
    }

    public static String getHexString(String value, String datatype, int length) {
        String hexStr = "";
        if ("str".equals(datatype)) {
            byte[] temp = stringToBytes(value);
            hexStr = bytesToHexString(temp);
        } else if ("hex".equals(datatype)) {
            hexStr = value;
        } else if ("int".equals(datatype)) {
            hexStr = Integer.toHexString(Integer.parseInt(value));
        }
        String zeroPrefix = "";
        if (hexStr.length() < length * 2) {
            for (int i = 0; i < length * 2 - hexStr.length(); i++) {
                zeroPrefix += "0";
            }
        }
        hexStr = zeroPrefix + hexStr;
        return hexStr.toUpperCase();
    }

    /**
     * 字符串转无符号短整数
     * 
     * @param value
     * @return
     */
    public static short strToUINT16(String value) {
        int temp = Integer.parseInt(value);
        return (short) (temp & 0xffff);
    }

    /**
     * 字符串转无符号整数
     * 
     * @param value
     * @return
     */
    public static int strToUINT32(String value) {
        long temp = Long.parseLong(value);
        return (int) (temp & 0xffffffff);
    }

    /**
     * String转16进制字符串
     * 
     * @param value
     * @return
     */
    public static String toHexString(String value) {
        return bytesToHexString(stringToBytes(value));

    }

    /**
     * short转16进制字符串
     * 
     * @param value
     * @return
     */
    public static String toHexString(short value) {
        return bytesToHexString(shortToBytes(value));

    }

    /**
     * int转16进制字符串
     * 
     * @param value
     * @return
     */
    public static String toHexString(int value) {
        return bytesToHexString(intToBytes(value));

    }

    /**
     * int转16进制字符串
     * 
     * @param value
     * @return
     */
    public static String toHexString(int value, short length) {
        String hexStr = bytesToHexString(intToBytes(value));
        hexStr = hexStr.substring(8 - length * 2);
        return hexStr;

    }

    /**
     * float转16进制字符串
     * 
     * @param value
     * @return
     */
    public static String toHexString(float value) {
        return bytesToHexString(floatToBytes(value));
    }

    /**
     * String转byte数组
     * 
     * @param intValue
     * @return
     */
    public static byte[] stringToBytes(String strValue, String charset) {
        try {
            return strValue.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] stringToBytes(String strValue) {
        return strValue.getBytes();
    }

    /**
     * float转byte数组
     * 
     * @param intValue
     * @return
     */
    public static byte[] floatToBytes(float floatValue) {
        int intValue = Float.floatToIntBits(floatValue);
        return intToBytes(intValue);
    }

    /**
     * int转byte数组
     * 
     * @param intValue
     * @return
     */
    public static byte[] intToBytes(int intValue) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (intValue >> 8 * (3 - i) & 0xFF);
        }
        return b;
    }

    /**
     * int转byte数组
     * 
     * @param intValue
     * @return
     */
    public static byte[] shortToBytes(short shortValue) {
        byte[] b = new byte[2];
        for (int i = 0; i < 2; i++) {
            b[i] = (byte) (shortValue >> 8 * (1 - i) & 0xFF);
        }
        return b;
    }

    /**
     * 将字节数组转成16进制字符串
     * 
     * @return
     */
    public static String bytesToHexString(byte[] bs) {
        if (bs == null || bs.length == 0) {
            return "";
        }
        StringBuffer str = new StringBuffer(bs.length * 4);
        for (int i = 0; i < bs.length; i++) {
            str.append(hex[(bs[i] >> 4) & 0x0f]);
            str.append(hex[bs[i] & 0x0f]);
        }
        return str.toString();
    }

    /**
     * 将字节数组转成字符串
     * 
     * @param bytes
     * @return
     */
    public static String byteToString(byte[] bytes) {
        return new String(bytes);
    }

    /**
     * 以16进制的方式按行打印字节数组
     * 
     * @param bs
     *            ：要打印的字节数组，lineLength：每行字节数
     * @return
     */
    public static String bytesToHexStringLine(byte[] bs, int lineLength) {
        if (bs == null || bs.length == 0) {
            return "";
        }
        StringBuffer str = new StringBuffer(bs.length * 4);
        for (int i = 0; i < bs.length; i++) {
            str.append(hex[(bs[i] >> 4) & 0x0f]);
            str.append(hex[bs[i] & 0x0f]);
            if (i > 0 && i % lineLength == lineLength - 1) {
                str.append("\r\n");
            } else {
                str.append(" ");
            }
        }
        return str.toString();
    }

    /**
     * 16进制字符串转可显示的字符串
     * 
     * @param hexStr
     * @return
     */
    public static String hexStrToStr(String hexStr) {
        String decStr = "";
        ByteBuffer bytes = ByteBuffer.allocate(hexStr.length() / 2);
        for (int i = 0; i < hexStr.length(); i += 2) {
            Byte b = (byte) (0xff & Integer.parseInt(hexStr.substring(i, i + 2), 16));
            bytes.put(b);
        }
        bytes.position();
        try {
            decStr = new String(bytes.array(), "GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return decStr;
    }

    public static byte[] hexToBytes(String hexStr) {
        ByteBuffer bytes = ByteBuffer.allocate(hexStr.length() / 2);
        for (int i = 0; i < hexStr.length(); i += 2) {
            Byte b = (byte) (0xff & Integer.parseInt(hexStr.substring(i, i + 2), 16));
            bytes.put(b);
        }
        return bytes.array();
    }

    public static long getLong(byte[] bytes) {
        long temp = 0;
        for (int i = bytes.length - 1; i >= 0; i--) {
            temp |= ((long) bytes[i] & 0xff) << (i * 8);
        }
        return temp;
    }

    public static int getInt(byte[] bytes) {
        int temp = 0;
        for (int i = bytes.length - 1; i >= 0; i--) {
            temp |= (bytes[i] & 0xff) << (i * 8);
        }
        return temp;
    }

    // *********************************end*********************************************************

    public static void main(String[] args) {
        byte[] bytes = new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        System.out.println(getLong(bytes));
        System.out.println(getInt(bytes));
    }
}
