package com.hiramgames.util;

import sun.misc.BASE64Encoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TokenUtil {
    public static String generateToken(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte md5[] =  md.digest(str.getBytes());
            BASE64Encoder encoder = new BASE64Encoder();
            return encoder.encode(md5);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
