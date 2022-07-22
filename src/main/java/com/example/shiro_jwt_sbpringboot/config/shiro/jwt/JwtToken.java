package com.example.shiro_jwt_sbpringboot.config.shiro.jwt;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * 重写Token
 */
public class JwtToken implements AuthenticationToken {
    /**
     * Token
     * @return
     */
    private String token;
    public JwtToken(String token){
        this.token=token;
    }


    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
