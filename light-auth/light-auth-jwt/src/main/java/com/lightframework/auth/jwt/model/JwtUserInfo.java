package com.lightframework.auth.jwt.model;

import com.lightframework.auth.common.model.UserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/***
 * @author yg
 * @date 2024/5/23 20:08
 * @version 1.0
 */
public class JwtUserInfo extends UserInfo implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(super.getPermissions() != null){
            return super.getPermissions().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public String getPassword() {
        return super.password();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return super.isEnable();
    }
}
