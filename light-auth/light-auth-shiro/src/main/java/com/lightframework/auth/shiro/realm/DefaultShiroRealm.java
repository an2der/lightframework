package com.lightframework.auth.shiro.realm;

import com.lightframework.auth.core.model.UserInfo;
import com.lightframework.auth.core.service.UserAuthService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/***
 * @author yg
 * @date 2023/11/6 19:15
 * @version 1.0
 */
@Component
public class DefaultShiroRealm extends AuthorizingRealm {

    @Autowired
    private UserAuthService userService;

    /**
     * 权限
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        UserInfo userInfo = (UserInfo) principalCollection.getPrimaryPrincipal();
        authorizationInfo.setStringPermissions(userInfo.getPermissions());
        return authorizationInfo;
    }

    /**
     * 登录
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        String username = (String) authenticationToken.getPrincipal();
        UserInfo userInfo = userService.getUserInfoByUsername(username);
        if(userInfo != null){
            return new SimpleAuthenticationInfo(userInfo,userInfo.password(),ByteSource.Util.bytes(userInfo.salt()),getName());
        }
        return null;
    }
}
