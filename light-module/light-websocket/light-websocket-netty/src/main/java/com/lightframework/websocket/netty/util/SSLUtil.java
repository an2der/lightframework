package com.lightframework.websocket.netty.util;

import com.lightframework.util.spring.SpringContextUtil;
import org.springframework.core.io.ResourceLoader;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.InputStream;
import java.security.KeyStore;

public class SSLUtil {

    public static SSLEngine createSSLEngine(String keystore,String password) throws Exception {
        InputStream ksInputStream = null;
        try {
            ResourceLoader resourceLoader = SpringContextUtil.getApplicationContext();
            ksInputStream = resourceLoader.getResource(keystore).getInputStream();
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(ksInputStream, password.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password.toCharArray());
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            sslEngine.setNeedClientAuth(false);
            return sslEngine;
        }finally {
            if(ksInputStream != null) {
                ksInputStream.close();
            }
        }
    }
}
