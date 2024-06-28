package com.lightframework.auth.jwt.config;

import com.lightframework.auth.core.properties.AuthConfigProperties;
import com.lightframework.auth.jwt.filter.JwtAuthenticationFilter;
import com.lightframework.auth.jwt.handler.AuthenticationHandler;
import com.lightframework.auth.jwt.properties.JwtAuthConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/***
 * @author yg
 * @date 2024/5/23 17:46
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthConfigProperties authConfigProperties;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder getPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expressionInterceptUrlRegistry = http
                // CSRF禁用，因为不使用session
                .csrf().disable()
                // 基于token，所以不需要session
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll();
        AuthConfigProperties.InterceptUrl interceptUrl = authConfigProperties.getConfiguration().getInterceptUrl();
        if(interceptUrl != null){
            //配置忽略路径
            if(interceptUrl.getExcludes() != null){
                expressionInterceptUrlRegistry.antMatchers(interceptUrl.getExcludes().stream().toArray(String[]::new)).permitAll();
            }
            //配置拦截路径
            if(interceptUrl.getIncludes() != null){
                expressionInterceptUrlRegistry.antMatchers(interceptUrl.getIncludes().stream().toArray(String[]::new)).authenticated();
            }
        }
        return expressionInterceptUrlRegistry.and().addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling().authenticationEntryPoint(new AuthenticationHandler()).and() //认证失败返回信息
//                .exceptionHandling().accessDeniedHandler(accessDeniedHandler).and() //授权失败 没有权限
                .cors().and()
                .build();
    }

}
