package com.hdd.common.encrypt;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hdd.common.util.ByteUtil;

public class TripleDes {
    private static Logger log = LoggerFactory.getLogger(TripleDes.class);
    // 默认加密算法
    private static final String DefaultAlgorithm = "DESede";
    // 默认Block模式
    private static final String DefaultBlockPattern = "ECB";
    // 默认Padding模式
    private static final String DefaultPaddingPattern = "PKCS5Padding";

    // 加密算法,可用DES,DESede,Blowfish
    private String algorithm;
    // Block模式
    private String blockPattern;
    // Padding模式
    private String paddingPattern;

    public TripleDes() {
        this.algorithm = DefaultAlgorithm;
        this.blockPattern = DefaultBlockPattern;
        this.paddingPattern = DefaultPaddingPattern;
    }

    public TripleDes(String algorithm, String blockPattern, String paddingPattern) {
        this.algorithm = algorithm;
        this.blockPattern = blockPattern;
        this.paddingPattern = paddingPattern;
    }

    /**
     * 
     * 用key加密字符串，key的字节长度必须是16或24或32位。字符串的编解码默认采用utf-8，若有汉字务必注意一个汉字占用3个字节
     * 
     * @author David
     * @since: 2012-4-6
     */
    public String encrypt(String src, String key) {
        byte[] srcbyte = ByteUtil.stringToBytes(src);
        byte[] keybyte = ByteUtil.stringToBytes(key);
        byte[] destByte = encrypt(srcbyte, keybyte);
        return Base64.encrypt(destByte);
    }

    /**
     * 
     * Description:
     * 
     * @author David
     * @since: 2012-4-6
     */
    public byte[] encrypt(byte[] src, byte[] key) {
        try {
            // 生成密钥
            SecretKey deskey = new SecretKeySpec(key, algorithm);
            // 加密
            Cipher c = Cipher.getInstance(algorithm + "/" + blockPattern + "/" + paddingPattern);
            c.init(Cipher.ENCRYPT_MODE, deskey);
            return c.doFinal(src);
        } catch (Exception e) {
            log.error("encrypt error", e);
        }
        return null;
    }

    /**
     * 
     * Description:
     * 
     * @author David
     * @since: 2012-4-6
     */
    public String decrypt(String src, String key) {
        byte[] srcbyte = Base64.decrypt(src);
        byte[] keybyte = ByteUtil.stringToBytes(key);
        byte[] destByte = decrypt(srcbyte, keybyte);
        return ByteUtil.byteToString(destByte);
    }

    /**
     * 
     * Description:
     * 
     * @author David
     * @since: 2012-4-6
     */
    public byte[] decrypt(byte[] src, byte[] key) {
        try {
            // 生成密钥
            SecretKey deskey = new SecretKeySpec(key, algorithm);
            // 解密
            Cipher c1 = Cipher.getInstance(algorithm + "/" + blockPattern + "/" + paddingPattern);
            c1.init(Cipher.DECRYPT_MODE, deskey);
            return c1.doFinal(src);
        } catch (java.lang.Exception e) {
            log.error("decrypt error", e);
        }
        return null;
    }
    
    public static void main(String args[]){
        TripleDes ed  = new TripleDes();
        System.out.print(ed.encrypt("hdd", "QE!@^&0(J6H#$%DRN*$v7rnt"));
    }

}
