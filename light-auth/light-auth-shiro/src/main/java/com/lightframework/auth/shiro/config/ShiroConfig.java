package com.lightframework.auth.shiro.config;

import com.lightframework.auth.core.model.AuthConfigProperties;
import com.lightframework.auth.shiro.realm.DefaultShiroRealm;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Autowired
    private AuthorizingRealm realm;

    @Autowired
    private AuthConfigProperties authConfigProperties;

    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm);
        securityManager.setSessionManager(defaultWebSessionManager());//配置session管理器
        return securityManager;
    }

    @Bean
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName(authConfigProperties.getPasswordCrypto().getHashAlgorithm());//散列算法
        hashedCredentialsMatcher.setHashIterations(authConfigProperties.getPasswordCrypto().getHashIterations());//散列的次数;
        return hashedCredentialsMatcher;
    }

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

        //设置安全管理器
        shiroFilterFactoryBean.setSecurityManager(securityManager());

        HashMap<String, Filter> myFilters = new HashMap<>();
        myFilters.put("authc", new ShiroAuthFilter());
        shiroFilterFactoryBean.setFilters(myFilters);

        //资源拦截器.
        Map<String,String> filterChainDefinitionMap = new LinkedHashMap<String,String>();
        AuthConfigProperties.InterceptUrl interceptUrl = authConfigProperties.getInterceptUrl();
        if(interceptUrl != null){
            //配置拦截路径
            if(interceptUrl.getIncludes() != null){
                interceptUrl.getIncludes().forEach(s -> filterChainDefinitionMap.put(s, "authc"));
            }
            //配置忽略路径
            if(interceptUrl.getExcludes() != null){
                interceptUrl.getExcludes().forEach(s -> filterChainDefinitionMap.put(s, "anon"));
            }
        }
        filterChainDefinitionMap.put("/api/auth/**", "anon");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }


    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    @Bean
    public DefaultWebSessionManager defaultWebSessionManager(){
        DefaultWebSessionManager defaultWebSessionManager = new WebSessionManager();
        if(authConfigProperties.getExpireTimeMinute() != null && authConfigProperties.getExpireTimeMinute().intValue() > 0) {
            defaultWebSessionManager.setGlobalSessionTimeout(authConfigProperties.getExpireTimeMinute().intValue() * 60 * 1000);//单位ms
        }
        return defaultWebSessionManager;
    }

}
