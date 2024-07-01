package com.lightframework.util.crypto;

/***
 * @author yg
 * @date 2023/11/7 17:42
 * @version 1.0
 */
public class CryptoUtil {

    private CryptoUtil(){}

    /**
     * 异或加密
     * @param str
     * @return
     */
    public static String XORCrypto(String str){
        if(str == null || str.length() == 0){
            return null;
        }
        int key = (int) Math.ceil(str.length() * 0.75);
        return XORCrypto(str,key);
    }

    /**
     * 异或加密
     * @param str
     * @param key
     * @return
     */
    public static String XORCrypto(String str, int key){
        if(str == null || str.length() == 0){
            return null;
        }
        char [] chars = str.toCharArray();
        char [] encryptedChars = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            encryptedChars[i] = (char) (chars[i] ^ key);
        }
        return new String(encryptedChars);
    }

    /**
     * 异或加密
     * @param bytes
     * @return
     */
    public static byte [] XORCrypto(byte [] bytes){
        if(bytes == null){
            return null;
        }
        int key = (int) Math.ceil(bytes.length * 0.75);
        return XORCrypto(bytes,key);
    }

    /**
     * 异或加密
     * @param bytes 数据
     * @param key 密钥
     * @return
     */
    public static byte [] XORCrypto(byte [] bytes, int key){
        if(bytes == null){
            return null;
        }
        byte [] encryptedBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            encryptedBytes[i] = (byte) (bytes[i] ^ key);
        }
        return encryptedBytes;
    }

}
