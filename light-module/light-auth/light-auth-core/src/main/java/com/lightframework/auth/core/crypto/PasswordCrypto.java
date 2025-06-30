package com.lightframework.auth.core.crypto;

public interface PasswordCrypto {

    String encryptPassword(String password,String salt);

    String encryptPassword(String password);
}
