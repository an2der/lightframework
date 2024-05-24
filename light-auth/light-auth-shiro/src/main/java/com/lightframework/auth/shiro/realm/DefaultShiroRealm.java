package com.lightframework.auth.shiro.realm;

import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.auth.core.service.UserAuthService;
import com.lightframework.auth.shiro.properties.ShiroAuthConfigProperties;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
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

    @Autowired
    private ShiroAuthConfigProperties authConfigProperties;

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
            return new SimpleAuthenticationInfo(userInfo,userInfo.password(),userInfo.salt() == null?ByteSource.Util.bytes(authConfigProperties.getSecret()):ByteSource.Util.bytes(userInfo.salt()),getName());
        }
        return null;
    }

    @Override
    public CredentialsMatcher getCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName(authConfigProperties.getPasswordCrypto().getHashAlgorithm());//散列算法
        hashedCredentialsMatcher.setHashIterations(authConfigProperties.getPasswordCrypto().getHashIterations());//散列的次数;
        return hashedCredentialsMatcher;
    }
}
