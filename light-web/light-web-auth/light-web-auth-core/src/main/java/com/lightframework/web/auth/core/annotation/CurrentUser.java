package com.lightframework.web.auth.core.annotation;

import java.lang.annotation.*;

/** 获取当前登录用户信息注解
 * @author yg
 * @date 2022/6/9 12:26
 * @version 1.0
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
