package com.example.shiro_jwt_sbpringboot.bean;

import com.example.shiro_jwt_sbpringboot.bean.entity.User;
import com.example.shiro_jwt_sbpringboot.bean.entity.UserRole;

import javax.persistence.Table;

@Table(name="user_role")
public class UserRoleDto extends UserRole {
}
