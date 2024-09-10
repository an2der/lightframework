package com.lightframework.auth.jwt.filter;

import com.lightframework.auth.jwt.model.JwtUserInfo;
import com.lightframework.auth.jwt.properties.JwtAuthConfigProperties;
import com.lightframework.auth.jwt.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/***
 * @author yg
 * @date 2024/5/23 18:37
 * @version 1.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtAuthConfigProperties authConfigProperties;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //获取token
        String token = request.getHeader(authConfigProperties.getConfiguration().getTokenKey());
        if(token == null){ //header不存在从cookie中获取
            Cookie[] cookies = request.getCookies();
            if(cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(authConfigProperties.getConfiguration().getTokenKey())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if(token!=null && token.startsWith(authConfigProperties.getTokenPrefix())) {
            try {
                Claims claims = jwtTokenUtil.getClaimsFormToken(token.replaceFirst(authConfigProperties.getTokenPrefix(),""));
                JwtUserInfo jwtUserInfo = new JwtUserInfo();
                jwtUserInfo.setUserId(claims.getId());
                jwtUserInfo.setUsername(claims.getSubject());
                jwtUserInfo.setPermissions(jwtTokenUtil.getPermission(claims));
                //存入SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(jwtUserInfo, null, jwtUserInfo.getAuthorities()));
            }catch (Exception e){

            }
        }
        filterChain.doFilter(request,response);
    }
}
