package com.example.shiro_jwt_sbpringboot.controller;

import com.example.shiro_jwt_sbpringboot.bean.UserDto;
import com.example.shiro_jwt_sbpringboot.bean.common.BaseDto;
import com.example.shiro_jwt_sbpringboot.bean.common.Constant;
import com.example.shiro_jwt_sbpringboot.bean.common.ResponseBean;
import com.example.shiro_jwt_sbpringboot.bean.valid.UserEditValidGroup;
import com.example.shiro_jwt_sbpringboot.bean.valid.UserLoginValidGroup;
import com.example.shiro_jwt_sbpringboot.exception.CustomException;
import com.example.shiro_jwt_sbpringboot.exception.CustomUnauthorizedException;
import com.example.shiro_jwt_sbpringboot.service.IUserService;
import com.example.shiro_jwt_sbpringboot.util.AesCipherUtil;
import com.example.shiro_jwt_sbpringboot.util.JedisUtil;
import com.example.shiro_jwt_sbpringboot.util.JwtUtil;
import com.example.shiro_jwt_sbpringboot.util.UserUtil;
import com.example.shiro_jwt_sbpringboot.util.common.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/user")
@PropertySource("classpath:config.properties")
public class UserController {

    /**
     * RefreshToken 过期时间
     */
    @Value("${refreshTokenExpireTime}")
    private String refreshTokenExpireTime;

    private final UserUtil userUtil;

    private final IUserService userService;

    @Autowired
    public UserController(UserUtil userUtil, IUserService userService) {
        this.userUtil = userUtil;
        this.userService = userService;
    }

    /**
     * 获取用户列表
     * @param baseDto
     * @return
     */
    @GetMapping
    @RequiresPermissions(logical= Logical.AND,value={"user:view"})
    public ResponseBean user(@Validated BaseDto baseDto){
        //页数和每页的条数
        if(baseDto.getPage()==null||baseDto.getRows()==null){
            baseDto.setPage(1);
            baseDto.setRows(10);
        }
        //设置初始页面（第几页）和每页的条数
        PageHelper.startPage(baseDto.getPage(),baseDto.getRows());
        List<UserDto> userDtos = userService.selectAll();
        //将页面数据放入其中
        PageInfo<UserDto> selectPage=new PageInfo<>(userDtos);
        if(userDtos==null||userDtos.size()<0){
            throw new CustomException("查询失败(Query Failure)");
        }

        Map<String,Object> result=new HashMap<String,Object>(16);
        result.put("count",selectPage.getTotal());
        result.put("data",selectPage.getList());
        return new ResponseBean(HttpStatus.OK.value(),"查询成功(Query was successful)", result);

    }

    /**
     * 获取在线的用户（查询redis中的RefreshToken）
     * @return
     */
    @GetMapping("/online")
    @RequiresPermissions(logical=Logical.AND,value={"user:view"})
    public ResponseBean online(){
        List<Object> userDtos=new ArrayList<Object>();
        //查询所有的redis键  (模糊查询 以RefreshToken开头的)
        Set<String> keys = JedisUtil.keysS(Constant.PREFIX_SHIRO_REFRESH_TOKEN + "*");
        for(String key : keys){
            if(JedisUtil.exists(key)){
                //根据:分割key 获取最后一个字符
                String[] strArray = key.split(":");
                UserDto userDto=new UserDto();
                userDto.setAccount(strArray[strArray.length-1]);
                userDto=userService.selectOne(userDto);
                //设置登录时间
                userDto.setLoginTime(new Date(Long.parseLong(JedisUtil.getObject(key).toString())));
                userDtos.add(userDto);
            }
        }
        if(userDtos==null||userDtos.size()<0){
            throw new CustomException("查询失败(Query Failure)");

        }
        return new ResponseBean(HttpStatus.OK.value(), "查询成功(Query was successful)", userDtos);

    }

    /**
     * 登录授权
     * @param userDto
     * @param response
     * @return
     */
    @PostMapping("/login")
    public ResponseBean login(@Validated (UserLoginValidGroup.class)UserDto userDto, HttpServletResponse response){
        //查询数据库中的账号信息
        UserDto userDtoTemp=new UserDto();
        userDtoTemp.setAccount(userDto.getAccount());
        userDtoTemp=userService.selectOne(userDtoTemp);
        if(userDtoTemp==null){
            throw new CustomUnauthorizedException("该帐号不存在(The account does not exist.)");
        }
        //密码进行AES解密
        String key = AesCipherUtil.deCrypto(userDtoTemp.getPassword());
        // 因为密码加密是以帐号+密码的形式进行加密的，所以解密后的对比是帐号+密码
        if(key.equals(userDto.getAccount()+userDto.getPassword())){
            //清除可能存在的shiro权限信息缓存
            if(JedisUtil.exists(Constant.PREFIX_SHIRO_CACHE+userDto.getAccount())){
                JedisUtil.delKey(Constant.PREFIX_SHIRO_CACHE+userDto.getAccount());
            }
            //设置RefreshToken 时间戳为当前的时间戳 直接设置即可 （不用先删除后设置 因为会自动覆盖）
            String currentTimeMillis = String.valueOf(System.currentTimeMillis());
            JedisUtil.setObject(Constant.PREFIX_SHIRO_REFRESH_TOKEN + userDto.getAccount(), currentTimeMillis, Integer.parseInt(refreshTokenExpireTime));
            //从Header中的Authorization返回AccessToken 时间戳为当前的时间戳
            String token = JwtUtil.sign(userDto.getAccount(), currentTimeMillis);
            response.setHeader("Authorization", token);
            response.setHeader("Access-Control-Expose-Headers", "Authorization");
            return new ResponseBean(HttpStatus.OK.value(), "登录成功(Login Success.)", null);
        } else {
            throw new CustomUnauthorizedException("帐号或密码错误(Account or Password Error.)");
        }



    }

    /**
     * 测试登录
     * @return
     */
    @RequestMapping("/article")
    public ResponseBean article(){
        Subject subject = SecurityUtils.getSubject();
        //登录了就返回TRUE
        if(subject.isAuthenticated()){
            return new ResponseBean(HttpStatus.OK.value(), "您已经登录了(You are already logged in)", null);
        } else {
            return new ResponseBean(HttpStatus.OK.value(), "你是游客(You are guest)", null);
        }
    }

    /**
     * 测试登录注解
     *
     * @return
     */
    @GetMapping("/article2")
    @RequiresAuthentication
    public ResponseBean requireAuth(){
        return new ResponseBean(HttpStatus.OK.value(), "您已经登录了(You are already logged in)", null);
    }

    /**
     * 获取当前的登录用户信息
     * @return
     */
    @GetMapping("/info")
    @RequiresAuthentication
    public ResponseBean info(){
        //获取当前的登录用户
        UserDto user = userUtil.getUser();
        //id
        Integer userId = userUtil.getUserId();
        //登录的用户Token
        String token = userUtil.getToken();
        //account
        String account = userUtil.getAccount();
        return new ResponseBean(HttpStatus.OK.value(), "您已经登录了(You are already logged in)", user);
    }

    /**
     * 获取指定的用户
     * @return
     */
    @GetMapping("/{id}")
    @RequiresPermissions(logical=Logical.AND,value={"user:view"})
    public ResponseBean findById(@PathVariable("id") Integer id){
        UserDto userDto = userService.selectByPrimaryKey(id);
        if(userDto==null){
            throw new CustomException("查询失败(Query Failure)");

        }
        return new ResponseBean(HttpStatus.OK.value(), "查询成功(Query was successful)", userDto);
    }


    /**
     * 新增用户
     * @param userDto
     * @return
     */
    @PostMapping()
    @RequiresPermissions(logical = Logical.AND, value = {"user:edit"})
    public ResponseBean add(@Validated(UserEditValidGroup.class) @RequestBody UserDto userDto){
        //判断当前的账号是否存在
        UserDto userDtoTemp=new UserDto();
        userDtoTemp.setAccount(userDto.getAccount());
        userDtoTemp=userService.selectOne(userDtoTemp);
        if(userDtoTemp!=null&& StringUtil.isNotBlank(userDtoTemp.getPassword())){
            throw new CustomUnauthorizedException("该帐号已存在(Account exist.)");
        }

        userDto.setRegTime(new Date());
        //密码以账号+密码的形式进行AES加密
        if(userDto.getPassword().length()>Constant.PASSWORD_MAX_LEN){
            throw new CustomException("密码最多8位(Password up to 8 bits.)");
        }
        String key = AesCipherUtil.enCrypto(userDto.getPassword() + userDto.getAccount());
        userDto.setPassword(key);
        int count = userService.insert(userDto);
        if(count<=0){
            throw new CustomException("新增失败(Insert Failure)");
        }
        return new ResponseBean(HttpStatus.OK.value(), "新增成功(Insert Success)", userDto);
    }

    /**
     * 更新用户
     * @param userDto
     * @return
     */
    @PutMapping
    @RequiresPermissions(logical = Logical.AND, value = {"user:edit"})
    public ResponseBean update(@Validated(UserEditValidGroup.class) @RequestBody UserDto userDto){
        //查询数据库密码
        UserDto userDtoTemp=new UserDto();
        userDtoTemp.setAccount(userDto.getAccount());
        //更改之前的用户数据
        userDtoTemp = userService.selectOne(userDtoTemp);
        if(userDtoTemp==null){
            throw new CustomUnauthorizedException("该帐号不存在(Account not exist.)");
        } else {
            userDto.setId(userDtoTemp.getId());
        }
        //如果不一样 就说明用户修改了密码 重新加密密码
        if(!userDtoTemp.getPassword().equals(userDto.getPassword())){
            if(userDto.getPassword().length()>Constant.PASSWORD_MAX_LEN){
                throw new CustomException("密码最多八位");
            }
            String key = AesCipherUtil.enCrypto(userDto.getAccount() + userDto.getPassword());
            userDto.setPassword(key);
        }
        int count = userService.updateByPrimaryKeySelective(userDto);
        if (count <= 0) {
            throw new CustomException("更新失败(Update Failure)");
        }
        return new ResponseBean(HttpStatus.OK.value(), "更新成功(Update Success)", userDto);



    }

    /**
     * 删除用户
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @RequiresPermissions(logical = Logical.AND, value = {"user:edit"})
    public ResponseBean delete(@PathVariable("id") Integer id){
        int count = userService.deleteByPrimaryKey(id);
        if (count <= 0) {
            throw new CustomException("删除失败，ID不存在(Deletion Failed. ID does not exist.)");
        }
        return new ResponseBean(HttpStatus.OK.value(), "删除成功(Delete Success)", null);

    }

    /**
     * 剔除在线用户
     * @param id
     * @return com.wang.model.common.ResponseBean
     * @author dolyw.com
     * @date 2018/9/6 10:20
     */
    @DeleteMapping("/online/{id}")
    @RequiresPermissions(logical = Logical.AND, value = {"user:edit"})
    public ResponseBean deleteOnline(@PathVariable("id") Integer id) {
        UserDto userDto = userService.selectByPrimaryKey(id);
        if (JedisUtil.exists(Constant.PREFIX_SHIRO_REFRESH_TOKEN + userDto.getAccount())) {
            if (JedisUtil.delKey(Constant.PREFIX_SHIRO_REFRESH_TOKEN + userDto.getAccount()) > 0) {
                return new ResponseBean(HttpStatus.OK.value(), "剔除成功(Delete Success)", null);
            }
        }
        throw new CustomException("剔除失败，Account不存在(Deletion Failed. Account does not exist.)");
    }








}
