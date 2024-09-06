package com.lightframework.auth.jwt.handler;

import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Shiro异常处理
 * @author yg
 * @date 2022/6/10 19:32
 * @version 1.0
 */
@RestControllerAdvice
@Order(1)
public class JwtExceptionHandler {

    /**
     * 无权访问
     * @param e
     * @return
     */
    @ExceptionHandler(AccessDeniedException.class)
    public void accessDeniedExceptionHandler(AccessDeniedException e){
        throw e;
    }
}
