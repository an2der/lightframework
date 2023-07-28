package com.lightframework.common.annotation;

import com.lightframework.common.BusinessType;

import java.lang.annotation.*;

/*** 系统日志注解
 * @author yg
 * @date 2023/7/27 20:39
 * @version 1.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SystemLogger {

    String operationDesc();

    BusinessType businessType() default BusinessType.OTHER;

    String moduleName() default "";

    String moduleKey() default "";
}
