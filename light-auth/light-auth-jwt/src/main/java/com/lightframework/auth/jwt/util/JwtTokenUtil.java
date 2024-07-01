package com.lightframework.auth.jwt.util;

import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.auth.jwt.properties.JwtAuthConfigProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/***
 * @author yg
 * @date 2024/7/1 15:55
 * @version 1.0
 */
@Component
public class JwtTokenUtil {

    @Autowired
    private JwtAuthConfigProperties authConfigProperties;

    public String generateToken(UserInfo userInfo) {
        Map<String,Object> claims = new HashMap<>();
        claims.put("userinfo",userInfo);
        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + authConfigProperties.getConfiguration().getExpireTimeMinute() * 60 * 1000))
                .setId(userInfo.getUserId())
                .setSubject(userInfo.getUsername())
//                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, authConfigProperties.getConfiguration().getSecret())
                .compact();
    }

    public Claims getClaimsFormToken(String token) {
        return Jwts.parser()
                .setSigningKey(authConfigProperties.getConfiguration().getSecret())
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 验证 token 是否过期
     * @return
     */
    public boolean isTokenExpired(Claims claims) {
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

}
