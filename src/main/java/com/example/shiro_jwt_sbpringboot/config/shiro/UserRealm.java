package com.example.shiro_jwt_sbpringboot.config.shiro;

import com.example.shiro_jwt_sbpringboot.bean.PermissionDto;
import com.example.shiro_jwt_sbpringboot.bean.RoleDto;
import com.example.shiro_jwt_sbpringboot.bean.UserDto;
import com.example.shiro_jwt_sbpringboot.bean.common.Constant;
import com.example.shiro_jwt_sbpringboot.config.shiro.jwt.JwtToken;
import com.example.shiro_jwt_sbpringboot.dao.PermissionMapper;
import com.example.shiro_jwt_sbpringboot.dao.RoleMapper;
import com.example.shiro_jwt_sbpringboot.dao.UserMapper;
import com.example.shiro_jwt_sbpringboot.util.JedisUtil;
import com.example.shiro_jwt_sbpringboot.util.JwtUtil;
import com.example.shiro_jwt_sbpringboot.util.common.StringUtil;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 自定义Realm
 */
@Service
public class UserRealm extends AuthorizingRealm {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRealm.class);

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    @Autowired
    public UserRealm(UserMapper userMapper, RoleMapper roleMapper, PermissionMapper permissionMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    /**
     * 大坑 必须重写此方法 不然会报错
     * @param token
     * @return
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 默认使用此方法进行用户名验证正确与否 错误抛出异常即可
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {

        String token = (String)authenticationToken.getCredentials();
        //解密获得account
        String account = JwtUtil.getClaim(token, Constant.ACCOUNT);
        //账号为空
        if(StringUtil.isBlank(account)){
            throw new AuthenticationException("Token中帐号为空(The account in Token is empty.)");

        }
        //查询用户是否存在
        UserDto userDto=new UserDto();
        userDto.setAccount(account);
        userDto= userMapper.selectOne(userDto);
        if (userDto == null) {
            throw new AuthenticationException("该帐号不存在(The account does not exist.)");
        }

        //开始认证 要AccessToken 认证通过 且Redis中存在RefreshToken 且两个的时间戳一致
        if(JwtUtil.verify(token)&& JedisUtil.exists(Constant.PREFIX_SHIRO_REFRESH_TOKEN+account)){
            //获取RefreshToken的时间戳（我们对RefreshToken的value值存为时间戳）
            String currentTimeMillsRedis = JedisUtil.getObject(Constant.PREFIX_SHIRO_REFRESH_TOKEN + account).toString();
            //获取AccessToken的时间戳 与RefreshToken的时间戳对比
            if(JwtUtil.getClaim(token,Constant.CURRENT_TIME_MILLIS).equals(currentTimeMillsRedis)){
                return new SimpleAuthenticationInfo(token,token,"userRealm");
            }
        }
        throw new AuthenticationException("Token已过期(Token expired or incorrect.)");
    }

    /**
     * 只有当需要检测该用户的权限时才会调用此方法  比如 checkRole  checkPermission 之类的
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo simpleAuthorizationInfo=new SimpleAuthorizationInfo();
        String account = JwtUtil.getClaim(principalCollection.toString(), Constant.ACCOUNT);
        UserDto userDto=new UserDto();
        userDto.setAccount(account);
        //查询用户角色
        List<RoleDto> roleByUser = roleMapper.findRoleByUser(userDto);

        for(RoleDto roleDto : roleByUser){
            //添加角色
            simpleAuthorizationInfo.addRole(roleDto.getName());

            //根据用户角色查询权限
            List<PermissionDto> permissionByRole = permissionMapper.findPermissionByRole(roleDto);
            for(PermissionDto permissionDto : permissionByRole){
                if(permissionDto!=null){
                    //添加权限
                    simpleAuthorizationInfo.addStringPermission(permissionDto.getPerCode());
                }
            }

        }

        return simpleAuthorizationInfo;

    }
}
