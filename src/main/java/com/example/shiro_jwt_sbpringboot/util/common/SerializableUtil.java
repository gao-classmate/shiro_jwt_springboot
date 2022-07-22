package com.example.shiro_jwt_sbpringboot.util.common;

import com.example.shiro_jwt_sbpringboot.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Serializable工具
 */
public class SerializableUtil {

    private SerializableUtil(){

    }

    private static final Logger logger = LoggerFactory.getLogger(SerializableUtil.class);

    /**
     * 序列化  java实体类对象---》二进制
     * @param object
     * @return
     */
    public static byte[] serializable(Object object){
        ByteArrayOutputStream baos=null;
        ObjectOutputStream oos=null;

        try {
            baos=new ByteArrayOutputStream();
            oos=new ObjectOutputStream(baos);
            oos.writeObject(object);
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("SerializableUtil工具类序列化出现IOException异常:{}", e.getMessage());
            throw new CustomException("SerializableUtil工具类序列化出现IOException异常:" + e.getMessage());
        } finally {
            try {
                if (oos != null) {
                    oos.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                logger.error("SerializableUtil工具类序列化出现IOException异常:{}", e.getMessage());
                throw new CustomException("SerializableUtil工具类序列化出现IOException异常:" + e.getMessage());
            }
        }

    }


    /**
     * 反序列化  二进制对象----》java bean实体类对象
     * @param bytes
     * @return
     */
    public static Object unserializable(byte[] bytes){
        ByteArrayInputStream bais=null;
        ObjectInputStream ois=null;

        try {
            bais=new ByteArrayInputStream(bytes);
            ois=new ObjectInputStream(bais);
            return ois.readObject();
        }  catch (ClassNotFoundException e) {
            logger.error("SerializableUtil工具类反序列化出现ClassNotFoundException异常:{}", e.getMessage());
            throw new CustomException("SerializableUtil工具类反序列化出现ClassNotFoundException异常:" + e.getMessage());
        } catch (IOException e) {
            logger.error("SerializableUtil工具类反序列化出现IOException异常:{}", e.getMessage());
            throw new CustomException("SerializableUtil工具类反序列化出现IOException异常:" + e.getMessage());
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
                if (bais != null) {
                    bais.close();
                }
            } catch (IOException e) {
                logger.error("SerializableUtil工具类反序列化出现IOException异常:{}", e.getMessage());
                throw new CustomException("SerializableUtil工具类反序列化出现IOException异常:" + e.getMessage());
            }
        }
    }
}
