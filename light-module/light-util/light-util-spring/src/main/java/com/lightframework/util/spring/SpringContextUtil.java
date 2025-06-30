package com.lightframework.util.spring;

import cn.hutool.extra.spring.SpringUtil;
import org.springframework.boot.SpringApplication;

/*** Spring Context 工具
 * @author yg
 * @date 2023/11/7 11:40
 * @version 1.0
 */
public class SpringContextUtil extends SpringUtil {

    public static void exit(){
        SpringApplication.exit(getApplicationContext(), () -> 0);
        System.exit(0);
    }
}
