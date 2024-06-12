package com.lightframework.auth.shiro.config;

import com.lightframework.auth.core.properties.AuthConfigProperties;
import com.lightframework.auth.shiro.properties.ShiroAuthConfigProperties;
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
    private ShiroAuthConfigProperties authConfigProperties;

    @Bean
    public SecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(realm);
        securityManager.setSessionManager(defaultWebSessionManager());//配置session管理器
        return securityManager;
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
        Map<String,String> filterChainDefinitionMap = new LinkedHashMap<>();
        filterChainDefinitionMap.put("/api/auth/**", "anon");
        AuthConfigProperties.InterceptUrl interceptUrl = authConfigProperties.getConfiguration().getInterceptUrl();
        if(interceptUrl != null){
            //配置忽略路径
            if(interceptUrl.getExcludes() != null){
                interceptUrl.getExcludes().forEach(s -> filterChainDefinitionMap.put(s, "anon"));
            }
            //配置拦截路径
            if(interceptUrl.getIncludes() != null){
                interceptUrl.getIncludes().forEach(s -> filterChainDefinitionMap.put(s, "authc"));
            }
        }
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
        DefaultWebSessionManager defaultWebSessionManager = new WebSessionManager(authConfigProperties);
        if(authConfigProperties.getConfiguration().getExpireTimeMinute() > 0) {
            defaultWebSessionManager.setGlobalSessionTimeout(authConfigProperties.getConfiguration().getExpireTimeMinute() * 60 * 1000);//单位ms
        }else {
            defaultWebSessionManager.setGlobalSessionTimeout(-1);//永不过期
        }
        return defaultWebSessionManager;
    }

}
