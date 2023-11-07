package com.lightframework.system.log.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightframework.auth.core.model.UserInfo;
import com.lightframework.auth.core.service.UserInfoService;
import com.lightframework.common.BusinessException;
import com.lightframework.common.BusinessStatus;
import com.lightframework.common.annotation.SystemLogger;
import com.lightframework.system.log.model.SystemLog;
import com.lightframework.system.log.service.ISystemLogService;
import com.lightframework.util.net.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/*** 
 * @author yg
 * @date 2023/7/27 20:39
 * @version 1.0
 */
@Aspect
@Component
@Slf4j
public class SystemLogAspect {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ISystemLogService systemLogService;

    @Autowired(required = false)
    private UserInfoService userInfoService;

    @Pointcut("@annotation(com.lightframework.common.annotation.SystemLogger)")
    public void systemLogPointCut(){
    }

    /**
     * 处理正常返回逻辑
     * @param joinPoint
     * @param result
     */
    @AfterReturning(pointcut = "systemLogPointCut()", returning = "result")
    public void returningHandler(JoinPoint joinPoint,Object result){
        systemLogHandler(joinPoint,BusinessStatus.SUCCESS);
    }
    /**
     * 处理异常返回逻辑
     * @param joinPoint
     * @param throwable
     */
    @AfterThrowing(value = "systemLogPointCut()", throwing = "throwable")
    public void throwingHandler(JoinPoint joinPoint,Throwable throwable){
        systemLogHandler(joinPoint,(throwable instanceof BusinessException)?BusinessStatus.FAIL:BusinessStatus.ERROR);
    }

    /**
     * 保存系统日志，全程异步处理不影响主业务
     * @param joinPoint
     * @param status
     */
    private void systemLogHandler(JoinPoint joinPoint, BusinessStatus status){
        Date date = new Date();
        executorService.execute(()->{
            SystemLogger logger = getAnnotation(joinPoint);
            SystemLog systemLog = new SystemLog();
            systemLog.setId(UUID.randomUUID().toString());
            if(userInfoService != null) {
                UserInfo userInfo = userInfoService.getUserInfo();
                systemLog.setUserId(userInfo.getUserId());
                systemLog.setUsername(userInfo.getUsername());
            }
            systemLog.setCreateTime(date);
            systemLog.setIpAddr(IPUtil.getRemoteIpAddr(((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest()));
            systemLog.setRequestParam(getArgsToJsonArrayString(joinPoint));
            systemLog.setExecuteResult(status.getCode());
            systemLog.setOperationDesc(logger.operationDesc());
            systemLog.setOperationType(logger.businessType().getCode());
            systemLog.setModuleKey(logger.moduleKey());
            systemLog.setModuleName(logger.moduleName());
            systemLogService.save(systemLog);
        });
    }

    private SystemLogger getAnnotation(JoinPoint joinPoint){
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        return method.getAnnotation(SystemLogger.class);
    }

    private String getArgsToJsonArrayString(JoinPoint joinPoint){
        try {
            if(joinPoint.getArgs() != null) {
                return objectMapper.writeValueAsString(Arrays.stream(joinPoint.getArgs()).filter(this::isNotFilterObject).collect(Collectors.toList()));
            }
        } catch (JsonProcessingException e) {
            log.error("系统日志AOP序列化参数为字符串时发生异常",e);
        }
        return null;
    }

    private boolean isNotFilterObject(Object o){
        return !(o instanceof UserInfo
                || o instanceof MultipartFile
                || o instanceof MultipartFile []
                || o instanceof HttpServletRequest
                || o instanceof HttpServletResponse);
    }

}
