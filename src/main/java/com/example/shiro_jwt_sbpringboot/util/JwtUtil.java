package com.example.shiro_jwt_sbpringboot.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.shiro_jwt_sbpringboot.bean.common.Constant;
import com.example.shiro_jwt_sbpringboot.exception.CustomException;
import com.example.shiro_jwt_sbpringboot.util.common.Base64ConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Date;

@Component
@EnableAutoConfiguration
@PropertySource("classpath:config.properties")
public class JwtUtil {
    private static final Logger logger=LoggerFactory.getLogger(JwtUtil.class);

    /**
     * 过期时间从配置文件获取
     */
    private static String accessTokenExpireTime;

    /**
     * JWT认证加密私钥(Base64加密)
     */
    private static String encryptJWTKey;

    @Value("${accessTokenExpireTime}")
    public void setAccessTokenExpireTime(String accessTokenExpireTime) {
        JwtUtil.accessTokenExpireTime = accessTokenExpireTime;
    }

    @Value("${encryptJWTKey}")
    public void setEncryptJWTKey(String encryptJWTKey) {
        JwtUtil.encryptJWTKey = encryptJWTKey;
    }

    /**
     * 检验token是否正确
     * @param token
     * @return
     */
    public static boolean verify(String token){

        try {
            //账号解密
            String secret=getClaim(token,Constant.ACCOUNT)+Base64ConvertUtil.decode(encryptJWTKey);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token);
            return true;
        } catch (UnsupportedEncodingException e) {
            logger.error("JWTToken认证解密出现UnsupportedEncodingException异常:{}", e.getMessage());
            throw new CustomException("JWTToken认证解密出现UnsupportedEncodingException异常:" + e.getMessage());

        }

    }

    /**
     * 获得Token中的信息无需secret解密也能获得
     * @param token
     * @param claim
     * @return java.lang.String
     * @author Wang926454
     * @date 2018/9/7 16:54
     */
    public static String getClaim(String token, String claim) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            // 只能输出String类型，如果是其他类型返回null
            return jwt.getClaim(claim).asString();
        } catch (JWTDecodeException e) {
            logger.error("解密Token中的公共信息出现JWTDecodeException异常:{}", e.getMessage());
            throw new CustomException("解密Token中的公共信息出现JWTDecodeException异常:" + e.getMessage());
        }
    }



    /**
     *  //生成签名 5min后过期
     * @param account  账号
     *
     * @return  返回加密的token
     */
    public static String sign(String account,String currentTimeMills)  {


        try {
            //账号加jwt私钥加密
            String secret = account + Base64ConvertUtil.decode(encryptJWTKey);
            // 此处过期时间是以毫秒为单位，所以乘以1000
            Date date=new Date(System.currentTimeMillis()+Long.parseLong(accessTokenExpireTime)*1000);
            Algorithm algorithm = Algorithm.HMAC256(secret);

            //附带账号信息
            return JWT.create()
                    .withClaim("account",account)
                    .withClaim("currentTimeMillis",currentTimeMills)
                    .withExpiresAt(date)
                    .sign(algorithm);
        } catch (UnsupportedEncodingException e) {
            logger.error("JWTToken加密出现UnsupportedEncodingException异常:{}", e.getMessage());
            throw new CustomException("JWTToken加密出现UnsupportedEncodingException异常:" + e.getMessage());

        }




    }



}
