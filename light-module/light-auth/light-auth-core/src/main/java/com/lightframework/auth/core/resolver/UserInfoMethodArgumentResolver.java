package com.lightframework.auth.core.resolver;

import com.lightframework.auth.common.model.UserInfo;
import com.lightframework.auth.common.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** 用户信息方法参数对象
 * @author yg
 * @date 2022/6/13 11:16
 * @version 1.0
 */
@Component
public class UserInfoMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UserInfoService userInfoService;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == UserInfo.class;
    }

    @Override
    public UserInfo resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory){
        return userInfoService.getUserInfo();
    }

}
