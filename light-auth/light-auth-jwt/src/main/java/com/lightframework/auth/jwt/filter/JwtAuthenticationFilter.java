package com.lightframework.auth.jwt.filter;

import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.auth.jwt.properties.JwtAuthConfigProperties;
import com.lightframework.auth.jwt.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
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

        if(token!=null && token.startsWith(authConfigProperties.getTokenPrefix())) {

            Claims claims = jwtTokenUtil.getClaimsFormToken(token.replaceFirst(authConfigProperties.getTokenPrefix(),""));
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(claims.getId());
            userInfo.setUsername(userInfo.getUsername());
            //存入SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userInfo, null));
        }
        filterChain.doFilter(request,response);
    }
}
