package com.example.shiro_jwt_sbpringboot.dao;

import com.example.shiro_jwt_sbpringboot.bean.PermissionDto;
import com.example.shiro_jwt_sbpringboot.bean.RoleDto;
import com.example.shiro_jwt_sbpringboot.bean.entity.Permission;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface PermissionMapper extends Mapper<PermissionDto> {
    /**
     * 根据Role查询Permission
     * @param roleDto
     * @return java.util.List<com.wang.model.PermissionDto>
     * @author dolyw.com
     * @date 2018/8/31 11:30
     */
    public List<PermissionDto> findPermissionByRole(RoleDto roleDto);
}