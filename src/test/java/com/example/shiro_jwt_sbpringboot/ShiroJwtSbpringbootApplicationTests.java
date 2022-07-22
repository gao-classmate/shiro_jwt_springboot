package com.example.shiro_jwt_sbpringboot;

import com.example.shiro_jwt_sbpringboot.util.AesCipherUtil;
import com.example.shiro_jwt_sbpringboot.util.common.Base64ConvertUtil;
import com.example.shiro_jwt_sbpringboot.util.common.HexConvertUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;

@SpringBootTest
@RunWith(SpringRunner.class)
class ShiroJwtSbpringbootApplicationTests {

    String pwd="QUJBNUYyM0M3OTNEN0I4MUFBOTZBOTkwOEI1NDI0MUE=";

    String secret="U0JBUElKV1RkV2FuZzkyNjQ1NA==";

    @Test
    void contextLoads() throws UnsupportedEncodingException {

      /*  String decode = Base64ConvertUtil.decode(secret);
        System.out.println(decode);*/
        System.out.println(AesCipherUtil.deCrypto("NDMyNDcwMDRENDE2NDRENTRFODZGNTc4MDNFQzBCN0U="));

        System.out.println(AesCipherUtil.enCrypto("adminadmin"));


    }
    @Test
    void test(){

        System.out.println(AesCipherUtil.deCrypto(pwd));

        System.out.println(AesCipherUtil.enCrypto("adminadmin"));


    }


}
