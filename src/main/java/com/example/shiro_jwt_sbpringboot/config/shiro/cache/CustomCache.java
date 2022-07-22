package com.example.shiro_jwt_sbpringboot.config.shiro.cache;

import com.example.shiro_jwt_sbpringboot.bean.common.Constant;
import com.example.shiro_jwt_sbpringboot.util.JedisUtil;
import com.example.shiro_jwt_sbpringboot.util.JwtUtil;
import com.example.shiro_jwt_sbpringboot.util.common.PropertiesUtil;
import com.example.shiro_jwt_sbpringboot.util.common.SerializableUtil;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * 重写Shiro的Cache保存读取和Shiro的Cache管理器
 *
 */
public class CustomCache<K,V> implements Cache<K,V> {

    /**
     * 缓存key名称获取为shiro：cache：account
     * @param
     * @return
     * @throws CacheException
     */
    private String getKey(Object key){
        return Constant.PREFIX_SHIRO_CACHE+ JwtUtil.getClaim(key.toString(),Constant.ACCOUNT);
    }

    /**
     * 获取缓存
     * @param key
     * @return
     * @throws CacheException
     */
    @Override
    public Object get(Object key) throws CacheException{
        if(Boolean.FALSE.equals(JedisUtil.exists(this.getKey(key)))){
            return null;
        }
        return JedisUtil.getObject(this.getKey(key));
    }

    /**
     *保存缓存
     * @param
     * @param
     * @return
     * @throws CacheException
     */
    @Override
    public Object put(Object key,Object value) throws CacheException {
        //读取配置文件 获取shiro的缓存过期时间
        PropertiesUtil.readProperties("config.properties");
        String shiroExpireTime = PropertiesUtil.getProperty("shiroCacheExpireTime");
        //设置redis的shiro缓存
        return JedisUtil.setObject(this.getKey(key),value, Integer.parseInt(shiroExpireTime));

    }

    /**
     * 移除缓存
     * @param
     * @return
     * @throws CacheException
     */
    @Override
    public Object remove(Object key) throws CacheException {
        if(Boolean.FALSE.equals(JedisUtil.exists(this.getKey(key)))){
            return null;
        }
        JedisUtil.delKey(this.getKey(key));
        return null;
    }

    /**
     * 清空所有的缓存
     * @throws CacheException
     */
    @Override
    public void clear() throws CacheException {
        //清除所有的缓存
        Objects.requireNonNull(JedisUtil.getJedis()).flushDB();
    }

    /**
     * 缓存的个数
     * @return
     */
    @Override
    public int size() {
        Long size=Objects.requireNonNull(JedisUtil.getJedis()).dbSize();
        return size.intValue();
    }

    /**
     * 获取所有的key
     * @return
     */
    @Override
    public Set keys() {
        Set<byte[]> keys = Objects.requireNonNull(JedisUtil.getJedis()).keys("*".getBytes());
        Set<Object> set=new HashSet<Object>();
        for(byte[] bs : keys){
            set.add(SerializableUtil.unserializable(bs));
        }
        return set;
    }

    /**
     * 获取所有的value
     * @return
     */
    @Override
    public Collection values() {
        Set keys = this.keys();
        List<Object> values=new ArrayList<Object>();
        for(Object key : keys){
            values.add(JedisUtil.getObject(this.getKey(key)));
        }
        return values;
    }
}
