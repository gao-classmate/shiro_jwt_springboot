package com.example.shiro_jwt_sbpringboot;

import com.example.shiro_jwt_sbpringboot.util.common.Base64ConvertUtil;

import java.io.UnsupportedEncodingException;

public class Test {
    String secret="U0JBUElKV1RkV2FuZzkyNjQ1NA==";
                  //VTBKQlVFbEtWMVJrVjJGdVp6a3lOalExTkE9PQ==
    String encode="VTBKQlVFbEtWMVJrVjJGdVp6a3lOalExTkE9PQ==";

    @org.junit.Test
    public void test() throws UnsupportedEncodingException {
        String str = Base64ConvertUtil.encode(secret);
        System.out.println(str);

        System.out.println("=======================分隔符号");
        System.out.println(Base64ConvertUtil.decode(secret));


    }
    @org.junit.Test
    public void decode(){
        try {
            System.out.println(Base64ConvertUtil.decode(encode));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
