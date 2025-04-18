package com.lightframework.auth.jwt.config;

import com.lightframework.auth.core.properties.AuthConfigProperties;
import com.lightframework.auth.core.service.AuthService;
import com.lightframework.auth.jwt.filter.JwtAuthenticationFilter;
import com.lightframework.auth.jwt.handler.JwtAccessDeniedHandler;
import com.lightframework.auth.jwt.handler.JwtAuthenticationHandler;
import com.lightframework.auth.jwt.properties.JwtAuthConfigProperties;
import com.lightframework.auth.jwt.service.impl.DefaultAuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
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
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthConfigProperties authConfigProperties;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    @ConditionalOnMissingBean(AuthService.class)
    public AuthService getAuthService(){
        return new DefaultAuthServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
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
                .exceptionHandling().authenticationEntryPoint(new JwtAuthenticationHandler()).and() //认证失败返回信息
                .exceptionHandling().accessDeniedHandler(new JwtAccessDeniedHandler()).and() //授权失败 没有权限
                .cors().and()
                .build();
    }

}
