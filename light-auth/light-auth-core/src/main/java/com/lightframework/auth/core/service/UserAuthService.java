package com.lightframework.auth.core.service;

import com.lightframework.auth.core.model.UserInfo;

/*** 
 * @author yg
 * @date 2023/11/6 19:26
 * @version 1.0
 */
public interface UserAuthService {
    
    UserInfo getUserInfoByUsername(String username);

}
