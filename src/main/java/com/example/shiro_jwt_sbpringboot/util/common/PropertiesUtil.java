package com.example.shiro_jwt_sbpringboot.util.common;

import com.example.shiro_jwt_sbpringboot.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * properties工具
 */
public class PropertiesUtil {
    private PropertiesUtil(){

    }
    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);


    private static final Properties prop=new Properties();


    public static void readProperties(String filename){
        InputStream in=null;
        try{
            in=PropertiesUtil.class.getResourceAsStream("/"+filename);
            BufferedReader bf=new BufferedReader(new InputStreamReader(in));
            prop.load(bf);
        }catch(IOException e){
            logger.error("PropertiesUtil工具类读取配置文件出现IOException异常:{}", e.getMessage());
            throw new CustomException("PropertiesUtil工具类读取配置文件出现IOException异常:" + e.getMessage());
        }finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                logger.error("PropertiesUtil工具类读取配置文件出现IOException异常:{}", e.getMessage());
                throw new CustomException("PropertiesUtil工具类读取配置文件出现IOException异常:" + e.getMessage());
            }
        }

    }

    //根矩key读取对应的value
    public static String getProperty(String key){
        return prop.getProperty(key);
    }
}
