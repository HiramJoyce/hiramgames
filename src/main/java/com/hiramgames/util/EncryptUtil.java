package com.hiramgames.util;

import java.math.BigInteger;
import java.security.MessageDigest;

public class EncryptUtil {
    private static final String KEY_SHA = "SHA-512";

    public static String SHA512(String inputStr) {
        BigInteger sha;
        byte[] inputData = inputStr.getBytes();
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(KEY_SHA);
            messageDigest.update(inputData);
            sha = new BigInteger(messageDigest.digest());
            return sha.toString(32);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
