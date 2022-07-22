package com.example.shiro_jwt_sbpringboot.dao;

import com.example.shiro_jwt_sbpringboot.bean.RoleDto;
import com.example.shiro_jwt_sbpringboot.bean.UserDto;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface RoleMapper extends Mapper<RoleDto> {
    /**
     * 根据User查询Role
     * @param userDto
     * @return java.util.List<com.wang.model.RoleDto>
     * @author dolyw.com
     * @date 2018/8/31 11:30
     */
    public List<RoleDto> findRoleByUser(UserDto userDto);
}