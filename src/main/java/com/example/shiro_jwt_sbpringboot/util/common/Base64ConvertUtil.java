package com.example.shiro_jwt_sbpringboot.util.common;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

//Base64工具
public class Base64ConvertUtil {

    private Base64ConvertUtil(){

    }

    //加密
    public static String encode(String str) throws UnsupportedEncodingException {
        byte[] encode = Base64.getEncoder().encode(str.getBytes("utf-8"));
        return new String(encode);
    }

    //解密
    public static String decode(String str) throws UnsupportedEncodingException {
        byte[] decode = Base64.getDecoder().decode(str.getBytes("utf-8"));
        return new String(decode);
    }

}
