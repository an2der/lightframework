package com.lightframework.web.auth.core.resolver;

import com.lightframework.web.auth.core.annotation.CurrentUser;
import com.lightframework.web.auth.core.model.UserInfo;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** 用户信息注解解析抽象
 * @author yg
 * @date 2022/6/13 11:16
 * @version 1.0
 */
public abstract class UserInfoMethodArgumentAnnotationResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class);
    }

    @Override
    public abstract UserInfo resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory);

}
