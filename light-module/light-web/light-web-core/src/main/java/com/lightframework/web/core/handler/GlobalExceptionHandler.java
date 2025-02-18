package com.lightframework.web.core.handler;

import cn.hutool.json.JSONUtil;
import com.lightframework.web.common.BusinessException;
import com.lightframework.web.common.BusinessResponse;
import com.lightframework.web.common.BusinessStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.stream.Collectors;

/** 全局异常处理
 * @author yg
 * @date 2022/6/10 19:32
 * @version 1.0
 */
@RestControllerAdvice
@Order(999)
public class GlobalExceptionHandler {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(BusinessException.class)
    public BusinessResponse businessExceptionHandler(BusinessException e){
        return new BusinessResponse(e);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public BusinessResponse bindExceptionHandler(BindException e){
        return new BusinessResponse(BusinessStatus.BAD_REQUEST.getCode(), e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(";")));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public BusinessResponse httpMessageNotReadableExceptionHandler(Exception e){
        return new BusinessResponse(BusinessStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public BusinessResponse constraintViolationExceptionHandler(ConstraintViolationException e){
        return new BusinessResponse(BusinessStatus.BAD_REQUEST.getCode(),e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage).collect(Collectors.joining(";")));
    }

    @ExceptionHandler(Exception.class)
    public BusinessResponse exceptionHandler(Exception e){
        log.error(e.getMessage(),e);
        return new BusinessResponse(BusinessStatus.ERROR);
    }
}
