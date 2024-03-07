package com.lightframework.util.spring;

import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

/*** Spring Property 工具
 * @author yg
 * @date 2023/11/7 11:40
 * @version 1.0
 */
@Component
public class SpringPropertyUtil implements EmbeddedValueResolverAware {

    private static StringValueResolver valueResolver = null;

    public static String getValue(String key){
        return valueResolver.resolveStringValue("${" + key + "}");
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        valueResolver = resolver;
    }
}
