package com.lightframework.util.crypto;

/*** 异或加密
 * @author yg
 * @date 2023/11/7 17:42
 * @version 1.0
 */
public class XORCrypto {

    private XORCrypto(){}

    /**
     * 加密字符串
     * @param str
     * @return
     */
    public static String encrypt(String str){
        if(str == null || str.length() == 0){
            return null;
        }
        int key = (int) Math.ceil(str.length() * 0.75);
        return encrypt(str,key);
    }

    /**
     * 加密字符串
     * @param str
     * @param secretKey 秘钥
     * @return
     */
    public static String encrypt(String str, int secretKey){
        if(str == null || str.length() == 0){
            return null;
        }
        char [] chars = str.toCharArray();
        char [] encryptedChars = new char[chars.length];
        for (int i = 0; i < chars.length; i++) {
            encryptedChars[i] = (char) (chars[i] ^ secretKey);
        }
        return new String(encryptedChars);
    }

    /**
     * 解密字符串
     * @param str
     * @return
     */
    public static String decrypt(String str){
        return encrypt(str);
    }

    /**
     * 解密字符串
     * @param str
     * @param secretKey 秘钥
     * @return
     */
    public static String decrypt(String str, int secretKey){
        return encrypt(str, secretKey);
    }

    /**
     * 加密byte数组
     * @param bytes
     * @return
     */
    public static byte [] encrypt(byte [] bytes){
        if(bytes == null){
            return null;
        }
        int key = (int) Math.ceil(bytes.length * 0.75);
        return encrypt(bytes,key);
    }

    /**
     * 加密byte数组
     * @param bytes
     * @param secretKey 秘钥
     * @return
     */
    public static byte [] encrypt(byte [] bytes, int secretKey){
        if(bytes == null){
            return null;
        }
        byte [] encryptedBytes = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            encryptedBytes[i] = (byte) (bytes[i] ^ secretKey);
        }
        return encryptedBytes;
    }

    /**
     * 解密byte数组
     * @param bytes
     * @return
     */
    public static byte [] decrypt(byte [] bytes){
        return encrypt(bytes);
    }

    /**
     * 解密byte数组
     * @param bytes
     * @param secretKey 秘钥
     * @return
     */
    public static byte [] decrypt(byte [] bytes, int secretKey){
        return encrypt(bytes, secretKey);
    }

}
