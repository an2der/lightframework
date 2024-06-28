package com.lightframework.auth.shiro.handler;

import com.lightframework.common.BusinessResponse;
import com.lightframework.common.BusinessStatus;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Shiro异常处理
 * @author yg
 * @date 2022/6/10 19:32
 * @version 1.0
 */
@RestControllerAdvice
@Order(1)
public class ShiroExceptionHandler {

    /**
     * 未登录
     * @param e
     * @return
     */
    @ExceptionHandler(UnauthenticatedException.class)
    public BusinessResponse unauthenticatedExceptionHandler(UnauthenticatedException e){
        return new BusinessResponse(BusinessStatus.UNAUTHORIZED);
    }

    /**
     * 无权访问
     * @param e
     * @return
     */
    @ExceptionHandler(UnauthorizedException.class)
    public BusinessResponse unauthorizedExceptionHandler(UnauthorizedException e){
        return new BusinessResponse(BusinessStatus.FORBIDDEN);
    }
}
