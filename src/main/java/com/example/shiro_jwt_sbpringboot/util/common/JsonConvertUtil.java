package com.example.shiro_jwt_sbpringboot.util.common;

import com.alibaba.fastjson.JSONObject;

/**
 * Json和Object的互相转换，转List必须Json最外层加[]，
 * 转Object，Json最外层不要加[]
 */
public class JsonConvertUtil {

    private JsonConvertUtil(){

    }

    /**
     * json转为Object
     * @param pojo
     * @param clazz
     * @param <T>
     * @return
     */
    public static<T> T jsonToObject(String pojo,Class<T> clazz){
        return JSONObject.parseObject(pojo,clazz);

    }

    /**
     * Object转为 json
     * @param t
     * @param <T>
     * @return
     */
    public static<T> String objectToJson(T t){
        return JSONObject.toJSONString(t);
    }


}
