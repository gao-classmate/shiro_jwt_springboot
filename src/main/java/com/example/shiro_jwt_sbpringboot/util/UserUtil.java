package com.example.shiro_jwt_sbpringboot.util;

import com.example.shiro_jwt_sbpringboot.bean.UserDto;
import com.example.shiro_jwt_sbpringboot.bean.common.Constant;
import com.example.shiro_jwt_sbpringboot.bean.entity.User;
import com.example.shiro_jwt_sbpringboot.dao.UserMapper;
import com.example.shiro_jwt_sbpringboot.exception.CustomException;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UserUtil {

    private final UserMapper userMapper;
    @Autowired
    public UserUtil(UserMapper userMapper){
        this.userMapper=userMapper;
    }

    /**
     * 获取当前的登录用户
     **/
    public UserDto getUser(){
        String token = SecurityUtils.getSubject().getPrincipal().toString();
        //获得解密Account
        String account = JwtUtil.getClaim(token, Constant.ACCOUNT);
        UserDto user=new UserDto();
        user.setAccount(account);
        user= userMapper.selectOne(user);
        if(user==null){
            throw new CustomException("该账号不存在");
        }
        return user;


    }

    /**
     * 获得当前用户的id
     * @return
     */
    public Integer getUserId(){
        return getUser().getId();
    }

    /**
     * 获得当前用户的token
     * @return
     */
    public String getToken(){
        return SecurityUtils.getSubject().getPrincipal().toString();

    }

    /**
     * 获得当前的登录用户Account
     * @return
     */
    public String getAccount(){
        String token = SecurityUtils.getSubject().getPrincipal().toString();
        //解密获得Account
        return JwtUtil.getClaim(token,Constant.ACCOUNT);
    }
}
