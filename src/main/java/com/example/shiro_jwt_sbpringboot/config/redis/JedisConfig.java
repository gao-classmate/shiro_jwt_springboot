package com.example.shiro_jwt_sbpringboot.config.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * jedis配置 项目启动时自动注入JedisPool
 */
@Configuration
@EnableAutoConfiguration
@ConfigurationProperties(prefix = "redis")
@PropertySource("classpath:config.properties")
public class JedisConfig {

    private static final Logger logger= LoggerFactory.getLogger(JedisConfig.class);

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.timeout}")
    private int timeout;

    @Value("${redis.password}")
    private String password;

    @Value("200")
    private int maxActive;
    @Value("-1")
    private int maxWait;
    @Value("8")
    private int maxIdle;
    @Value("0")
    private int minIdle;

    @Bean
    public JedisPool redisPoolFactory(){
        try{
            JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(maxIdle);
            jedisPoolConfig.setMaxWait(Duration.ofDays(maxWait));
            jedisPoolConfig.setMaxTotal(maxActive);
            jedisPoolConfig.setMinIdle(minIdle);

            JedisPool jedisPool=new JedisPool(jedisPoolConfig,host,port,timeout,password);
            logger.info("JedisPoll注入成功");
            logger.info("redis地址："+host+":"+port);
            return jedisPool;



        }catch(Exception e){
            logger.error("初始化Redis连接池JedisPool异常:{}", e.getMessage());
        }
        return null;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }


}
