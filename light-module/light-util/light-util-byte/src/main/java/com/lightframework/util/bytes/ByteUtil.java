package com.lightframework.util.bytes;

import cn.hutool.core.util.ZipUtil;
import com.lightframework.util.crypto.XORCrypto;

public class ByteUtil {
    private ByteUtil(){}

    public static String bytesToHexString(byte[] bytes) {
        if(bytes != null && bytes.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append("," + String.format("%02x", bytes[i]).toUpperCase());
            }
            return "[" + sb.substring(1) + "]";
        }
        return null;
    }

    public static byte [] cryptCompress(byte [] bytes){
        return XORCrypto.encrypt(ZipUtil.gzip(bytes));
    }

    public static byte [] decryptDecompress(byte [] bytes){
        return ZipUtil.unGzip(XORCrypto.decrypt(bytes));
    }
}
