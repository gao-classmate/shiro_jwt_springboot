package com.example.shiro_jwt_sbpringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.example.shiro_jwt_sbpringboot.dao")
public class ShiroJwtSbpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShiroJwtSbpringbootApplication.class, args);
    }

}
