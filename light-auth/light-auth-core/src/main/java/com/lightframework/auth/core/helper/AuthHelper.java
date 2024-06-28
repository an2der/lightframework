package com.lightframework.auth.core.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightframework.common.BusinessResponse;
import com.lightframework.common.BusinessStatus;
import org.springframework.http.MediaType;

import javax.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/***
 * @author yg
 * @date 2024/6/28 17:32
 * @version 1.0
 */
public final class AuthHelper {

    private AuthHelper(){}

    public static void responseUnauthorized(ServletResponse response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(mapper.writeValueAsString(new BusinessResponse(BusinessStatus.UNAUTHORIZED)));
        response.getWriter().flush();
        response.getWriter().close();
    }

    public static void responseForbidden(ServletResponse response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(mapper.writeValueAsString(new BusinessResponse(BusinessStatus.FORBIDDEN)));
        response.getWriter().flush();
        response.getWriter().close();
    }
}
