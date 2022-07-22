package com.example.shiro_jwt_sbpringboot.config.shiro.jwt;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.example.shiro_jwt_sbpringboot.bean.common.Constant;
import com.example.shiro_jwt_sbpringboot.bean.common.ResponseBean;
import com.example.shiro_jwt_sbpringboot.exception.CustomException;
import com.example.shiro_jwt_sbpringboot.util.JedisUtil;
import com.example.shiro_jwt_sbpringboot.util.JwtUtil;
import com.example.shiro_jwt_sbpringboot.util.common.JsonConvertUtil;
import com.example.shiro_jwt_sbpringboot.util.common.PropertiesUtil;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * jwt过滤  方法执行流程 preHandle->isAccessAllowed->isLoginAttempt->executeLogin
 */
public class JwtFilter extends BasicHttpAuthenticationFilter {


    private static final Logger logger=LoggerFactory.getLogger(JwtFilter.class);


    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        /**
         * 跨域已经在OriginFilter处全局配置
         */
        return super.preHandle(request,response);


    }

    /**
     * 这里我们详细说明下为什么最终返回的都是true，即允许访问
     * 例如我们提供一个地址 GET /article
     * 登入用户和游客看到的内容是不同的
     * 如果在这里返回了false，请求会被直接拦截，用户看不到任何东西
     * 所以我们在这里返回true，Controller中可以通过 subject.isAuthenticated() 来判断用户是否登入
     * 如果有些资源只有登入用户才能访问，我们只需要在方法上面加上 @RequiresAuthentication 注解即可
     * 但是这样做有一个缺点，就是不能够对GET,POST等请求进行分别过滤鉴权(因为我们重写了官方的方法)，但实际上对应用影响不大
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        //查看前端Header 中是否携带Authorization属性(Token) 有的话就进行登录认证授权
        if(this.isLoginAttempt(request,response)){
            try {
                //进行shiro的登录UserRealm
                this.executeLogin(request, response);
            } catch (Exception e) {
                //认证出现异常  传递错误信息msg
                String msg = e.getMessage();
                //获取应用异常（该cause是导致抛出此异throwable异常 ）
                Throwable cause = e.getCause();
                if(cause instanceof SignatureVerificationException){
                    //该异常为jwt的AccessToken认证失败（Token或者密匙不对）
                    msg= "Token或者密匙不对(" + cause.getMessage() + ")";

                }else if(cause instanceof TokenExpiredException){
                    //该异常为Token已经过期   判断RefreshToken未过期就进行AccessToken刷新
                    if(this.refreshToken(request,response)){
                        return true;
                    }else{
                        msg="Token已经过期("+ cause.getMessage()+")";
                    }
                }else{
                    //应用异常不为空
                    if(cause!=null){
                        //获取应用异常msg
                        msg= cause.getMessage();
                    }
                }
                // Token认证失败直接返回Response信息
                this.response401(response,msg);
                return false;
            }
        }else{
            // 没有携带Token
            HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
            // 获取当前请求类型
            String httpMethod = httpServletRequest.getMethod();
            // 获取当前请求URI
            String requestURI = httpServletRequest.getRequestURI();
            logger.info("当前请求 {} Authorization属性(Token)为空 请求类型 {}", requestURI, httpMethod);
            // mustLoginFlag = true 开启任何请求必须登录才可访问
            final Boolean mustLoginFlag = false;
            if (mustLoginFlag) {
                this.response401(response, "请先登录");
                return false;
            }
        }
        return true;
    }


    /**
     * 这里我们详细说明下为什么重写
     * 可以对比父类方法，只是将executeLogin方法调用去除了
     * 如果没有去除将会循环调用doGetAuthenticationInfo方法
     */
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        this.sendChallenge(request, response);
        return false;
    }

    /**
     * 检测Header里面是否有Authorization字段  有就进行Token登录认证授权
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
    //拿到当前Header中的Authorization的AccessToken（shiro中的getAuthZHeader方法已经实现·）
        String token = this.getAuthzHeader(request);
        return token !=null;
    }

    /**
     * 进行AccessToken登录认证授权
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        // 拿到当前Header中Authorization的AccessToken(Shiro中getAuthzHeader方法已经实现)
        JwtToken token = new JwtToken(this.getAuthzHeader(request));
        // 提交给UserRealm进行认证，如果错误他会抛出异常并被捕获
        this.getSubject(request, response).login(token);
        // 如果没有抛出异常则代表登入成功，返回true
        return true;
    }

    /**
     * 此处为AccessToken刷新 进行判断RefreshToken是否过期 未过期就返回AccessToken且继续正常访问
     * @param request
     * @param response
     * @return
     */
    private boolean refreshToken(ServletRequest request,ServletResponse response){
        //拿到当前Header中的Authorization的AccessToken
        String token = this.getAuthzHeader(request);
        //获取当前Token的账号信息、
        String account = JwtUtil.getClaim(token, Constant.ACCOUNT);
        //判断redis中的RefreshToken是否存在
        if(JedisUtil.exists(Constant.PREFIX_SHIRO_REFRESH_TOKEN+account)){
            // Redis中RefreshToken还存在，获取RefreshToken的时间戳
            String currentTimeMillisRedis = JedisUtil.getObject(Constant.PREFIX_SHIRO_REFRESH_TOKEN + account).toString();
            //  获取当前AccessToken中的时间戳，与RefreshToken的时间戳对比，如果当前时间戳一致，进行AccessToken刷新
            if(JwtUtil.getClaim(token,Constant.CURRENT_TIME_MILLIS).equals(currentTimeMillisRedis)){
                //获取当前最新时间戳
                String currentTimeMills = String.valueOf(System.currentTimeMillis());
                //读取配置文件 获取RefreshToken expireTime 属性
                PropertiesUtil.readProperties("config.properties");
                String refreshTokenExpireTime = PropertiesUtil.getProperty("refreshTokenExpireTime");
                //设置RefreshToken中的时间戳为当前的最新时间戳 且刷新过期时间为30分钟过期（配置文件可配置RefreshToken expireTime 属性）
                JedisUtil.setObject(Constant.PREFIX_SHIRO_REFRESH_TOKEN+account,currentTimeMills, Integer.parseInt(refreshTokenExpireTime));
                //刷新accessToken 设置时间戳为当前的最新时间戳
                token= JwtUtil.sign(account, currentTimeMills);
                //将刷新的AccessToken再次进行 shiro登录
                JwtToken jwtToken=new JwtToken(token);
                //// 提交给UserRealm进行认证，如果错误他会抛出异常并被捕获，如果没有抛出异常则代表登入成功，返回true
                this.getSubject(request,response).login(jwtToken);
                //最后刷新的AccessToken 存放在 response 的header 中的Authorization字段返回
                HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
                httpServletResponse.setHeader("Authorization", token);
                httpServletResponse.setHeader("Access-Control-Expose-Headers", "Authorization");
                return true;
            }

        }
        return false;
    }


    /**
     * 无需转发 直接返回 Response信息
     */
    private void response401(ServletResponse response,String msg){
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json; charset=utf-8");
        try (PrintWriter out = httpServletResponse.getWriter()) {
            String data = JsonConvertUtil.objectToJson(new ResponseBean(HttpStatus.UNAUTHORIZED.value(), "无权访问(Unauthorized):" + msg, null));
            out.append(data);
        } catch (IOException e) {
            logger.error("直接返回Response信息出现IOException异常:{}", e.getMessage());
            throw new CustomException("直接返回Response信息出现IOException异常:" + e.getMessage());
        }
    }
}
