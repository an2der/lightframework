package com.lightframework.util.spring.web;

import com.lightframework.util.spring.SpringJacksonUtil;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/***
 * @author yg
 * @date 2023/11/7 12:01
 * @version 1.0
 */
public class SpringServletUtil {

    private SpringServletUtil(){}

    public static HttpServletRequest getRequest(){
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static HttpServletResponse getResponse(){
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    public static void responseJSONStr(Object o) throws IOException {
        responseJSONStr(getResponse(),o);
    }

    public static void responseJSONStr(ServletResponse response, Object o) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(SpringJacksonUtil.serialize(o));
        response.getWriter().flush();
        response.getWriter().close();
    }

    public static String getCookie(String key){
        Cookie[] cookies = getRequest().getCookies();
        if(cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(key)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
