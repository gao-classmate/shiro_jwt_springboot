package com.example.shiro_jwt_sbpringboot.exception;

/**
 * 自定义401无权限异常
 */
public class CustomUnauthorizedException extends RuntimeException{

    public CustomUnauthorizedException(String msg){
        super(msg);

    }
    public CustomUnauthorizedException() {
        super();
    }

}
