package com.example.shiro_jwt_sbpringboot.bean;

import com.example.shiro_jwt_sbpringboot.bean.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
@Table(name="user")
public class UserDto extends User {
    /**
     * 登录时间 java 的transient关键字的作用是需要实现Serilizable接口，
     * 将不需要序列化的属性前添加关键字transient，序列化对象的时候，
     * 这个属性就不会序列化到指定的目的地中。
     */
    @Transient
    @JsonFormat(pattern ="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8" )
    private Date loginTime;

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }
}
