package com.lightframework.web.core.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;

import java.lang.annotation.*;

/** 业务controller，统一返回BusinessResponse对象
 * @author yg
 * @date 2022/6/13 14:45
 * @version 1.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
public @interface BusinessController {
    @AliasFor(
            annotation = Controller.class
    )
    String value() default "";
}
