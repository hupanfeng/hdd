package com.hdd.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPUtil {

    /**
     * 将字符串形式的ip地址转换为hex形式字符串，支持IPV4和IPV6两个版本
     * 
     * @param ip
     *            字符串形式的ip地址
     * @return hex字符串
     */
    public static String ipToHex(String ip) {
        ip = ip.replace(" ", "");
        int endIndex = ip.indexOf("/");
        if (endIndex > 0) {
            ip = ip.substring(0, endIndex);
        }
        String hexs;
        if (ip.contains(":")) {
            hexs = ipv6ToHex(ip);
        } else {
            hexs = ipv4ToHex(ip);
        }
        return hexs;
    }
    /**
     * 将字符串形式的ip地址转换为byte数组，支持IPV4和IPV6两个版本
     * 
     * @param ip
     *            字符串形式的ip地址
     * @return hex字符串
     */
    public static byte[] ipToBytes(String ip) {
        ip = ip.replace(" ", "");
        int endIndex = ip.indexOf("/");
        if (endIndex > 0) {
            ip = ip.substring(0, endIndex);
        }
        byte[] bytes;
        if (ip.contains(":")) {
            bytes = ipv6ToBytes(ip);
        } else {
            bytes = ipv4ToBytes(ip);
        }
        return bytes;
    }

    /**
     * 将hex形式 的ip数据转成字符串形式的ip地址，支持IPV4和IPV6两个版本
     * 
     * @param hex
     *            将hex形式 的ip数据
     * @return ip地址
     */
    public static String hexToIP(String hex) throws UnknownHostException {
        byte[] unsignedBytes = ByteUtil.hexToBytes(hex);
        // 去除符号位
        try {
            String ip = InetAddress.getByAddress(unsignedBytes).toString();
            return ip.substring(ip.indexOf('/') + 1).trim();
        } catch (UnknownHostException e) {
            throw e;
        }
    }

    public static String byteToIP(byte[] bytes) throws UnknownHostException {
        byte[] unsignedBytes = bytes;
        // 去除符号位
        try {
            String ip = InetAddress.getByAddress(unsignedBytes).toString();
            return ip.substring(ip.indexOf('/') + 1).trim();
        } catch (UnknownHostException e) {
            throw e;
        }
    }

    public static String ipv6ToHex(String ipv6) {
        byte[] bytes = ipv6ToBytes(ipv6);
        return ByteUtil.bytesToHexString(bytes);
    }

    /**
     * 字符串形式的ipv6地址hex形式的字符串
     * 
     * @param ipv6
     *            字符串形式的IP地址
     * @return 长度为32的hex形式的字符串
     */
    public static byte[] ipv6ToBytes(String ipv6) {
        byte[] ret = new byte[16];
        // ret[0] = 0;
        int ib = 15;
        // ipv4混合模式标记
        boolean comFlag = false;
        // 去掉开头的冒号
        if (ipv6.startsWith(":")) {
            ipv6 = ipv6.substring(1);
        }
        String groups[] = ipv6.split(":");
        // 反向扫描
        for (int ig = groups.length - 1; ig > -1; ig--) {
            if (groups[ig].contains(".")) {
                // 出现ipv4混合模式
                byte[] temp = ipv4ToBytes(groups[ig]);
                ret[ib--] = temp[3];
                ret[ib--] = temp[2];
                ret[ib--] = temp[1];
                ret[ib--] = temp[0];
                comFlag = true;
            } else if ("".equals(groups[ig])) {
                // 出现零长度压缩,计算缺少的组数
                int zlg = 9 - (groups.length + (comFlag ? 1 : 0));
                // 将这些组置0
                while (zlg-- > 0) {
                    ret[ib--] = 0;
                    ret[ib--] = 0;
                }
            } else {
                int temp = Integer.parseInt(groups[ig], 16);
                ret[ib--] = (byte) temp;
                ret[ib--] = (byte) (temp >> 8);
            }
        }
        return ret;
    }

    public static String ipv4ToHex(String ipv4) {
        byte[] bytes = ipv4ToBytes(ipv4);
        return ByteUtil.bytesToHexString(bytes);
    }

    /**
     * ipv4地址转有符号byte[5]
     * 
     * @param ipv4
     *            字符串的IPV4地址
     * @return big integer number
     */
    public static byte[] ipv4ToBytes(String ipv4) {
        String[] groups = ipv4.split("\\.");
        byte[] ret = new byte[4];
        for (int i = 0; i < 4; i++) {
            ret[i] = (byte) Integer.parseInt(groups[i]);
        }
        return ret;
    }

    /**
     * 测试程序
     * 
     * @param args
     */
    public static void main(String args[]) {

        String ipv4 = new String("254.0.0.2");
        System.out.println(IPUtil.ipToHex(ipv4));
        String myipv4;
        try {
            myipv4 = IPUtil.hexToIP(IPUtil.ipToHex(ipv4));
            System.out.println("The ipv4 =" + myipv4);
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        String ipv6 = "::ffff:10.116.49.240";
        System.out.println(IPUtil.ipToHex(ipv6));
        String myip6;
        try {
            myip6 = IPUtil.hexToIP(IPUtil.ipToHex(ipv6));
            System.out.println("The IPv6 =" + myip6);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
