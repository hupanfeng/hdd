package com.hdd.common.encrypt;

public class Base64 {
    private static char Base64Code[] = {'N', 'Q', 'T', 'W', 'a', 'd', 'g', 'j', 'n', 'q', 't', 'x', '0', '3', '6', '+', 'B', 'E', 'H', 'L', 'O', 'R', 'U', 'Y', 'b', 'e', 'i', 'l', 'o', 'r', 'v', 'y',
        '1', '4', '8', '/', 'C', 'F', 'J', 'M', 'P', 'S', 'X', 'Z', 'c', 'h', 'k', 'm', 'p', 'u', 'w', 'z', '2', '7', '9', 'A', 'D', 'I', 'K', 'V', 'f', 's', '5', 'G'};
    private static byte Base64Decode[] = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, 15, -1, -1, -1, 35, 12, 32, 52, 13, 33, 62, 14, 53, 34, 54, -1, -1, -1, -1, -1, -1, -1, 55, 16, 36, 56, 17, 37, 63, 18, 57, 38, 58, 19, 39, 0, 20, 40, 1, 21, 41, 2, 22,
        59, 3, 42, 23, 43, -1, -1, -1, -1, -1, -1, 4, 24, 44, 5, 25, 60, 6, 45, 26, 7, 46, 27, 47, 8, 28, 48, 9, 29, 61, 10, 49, 30, 50, 11, 31, 51, -1, -1, -1, -1, -1};

    public static int getEncodeRequriedLength(int i) {
        return ((i + 2) / 3) * 4;
    }

    public static String encrypt(byte abyte0[]) {
        int i = 0;
        StringBuffer stringbuffer = new StringBuffer((abyte0.length - 1) / 3 << 6);
        for (int j = 0; j < abyte0.length; j++) {
            i |= abyte0[j] << 16 - (j % 3) * 8 & 255 << 16 - (j % 3) * 8;
            if (j % 3 == 2 || j == abyte0.length - 1) {
                stringbuffer.append(Base64Code[(i & 0xfc0000) >> 18]);
                stringbuffer.append(Base64Code[(i & 0x3f000) >> 12]);
                stringbuffer.append(Base64Code[(i & 0xfc0) >> 6]);
                stringbuffer.append(Base64Code[i & 0x3f]);
                i = 0;
            }
        }

        if (abyte0.length % 3 > 0) {
            stringbuffer.setCharAt(stringbuffer.length() - 1, '=');
        }
        if (abyte0.length % 3 == 1) {
            stringbuffer.setCharAt(stringbuffer.length() - 2, '=');
        }
        return stringbuffer.toString();
    }

    public static int getDecodeRequriedLength(String s, int i) {
        int j = 0;
        if (s != null && !s.equals("")) {
            if (i % 4 != 0) {
                return -1;
            }
            if (s.charAt(i - 1) == '=') {
                j++;
            }
            if (s.charAt(i - 2) == '=') {
                j++;
            }
        }
        return (i / 4) * 3 - j;
    }

    public static byte[] decrypt(String s) {
        if (s == null) {
            return null;
        }
        int i = s.length();
        if (i % 4 != 0) {
            throw new IllegalArgumentException("Base64 string length must be 4*n");
        }
        if (i == 0) {
            return new byte[0];
        }
        int j = 0;
        if (s.charAt(i - 1) == '=') {
            j++;
        }
        if (s.charAt(i - 2) == '=') {
            j++;
        }
        int k = (i / 4) * 3 - j;
        byte abyte0[] = new byte[k];
        for (int l = 0; l < i; l += 4) {
            int i1 = ((l + 1) / 4) * 3;
            int j1 = 0;
            char c = s.charAt(l);
            if (Base64Decode[c] != -1) {
                j1 |= Base64Decode[c] << 18;
            }
            c = s.charAt(l + 1);
            if (Base64Decode[c] != -1) {
                j1 |= Base64Decode[c] << 12;
            }
            c = s.charAt(l + 2);
            if (Base64Decode[c] != -1) {
                j1 |= Base64Decode[c] << 6;
            }
            c = s.charAt(l + 3);
            if (Base64Decode[c] != -1) {
                j1 |= Base64Decode[c];
            }
            abyte0[i1] = (byte) ((j1 & 0xff0000) >> 16);
            if (i1 + 1 < k) {
                abyte0[i1 + 1] = (byte) ((j1 & 0xff00) >> 8);
            }
            if (i1 + 2 < k) {
                abyte0[i1 + 2] = (byte) (j1 & 0xff);
            }
        }
        return abyte0;
    }
}