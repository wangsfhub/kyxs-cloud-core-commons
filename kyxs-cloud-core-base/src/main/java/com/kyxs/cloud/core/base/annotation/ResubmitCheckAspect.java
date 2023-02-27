package com.kyxs.cloud.core.base.annotation;

import com.kyxs.cloud.core.base.config.RedisLock;
import com.kyxs.cloud.core.base.exception.BusinessException;
import com.kyxs.cloud.core.base.result.R;
import com.kyxs.cloud.core.base.utils.UserInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author wangsf
 * @date 2023/2/25
 * @description 防重复提交切面
 */
@Slf4j
@Aspect
@Component
public class ResubmitCheckAspect {
    @Autowired
    private RedisLock redisLock;

    @Resource
    private HttpServletRequest request;

    @Pointcut("execution(public * *(..)) && @annotation(com.kyxs.cloud.core.base.annotation.RepeatSubmit)")
    public void methodPointCut(){}

    /**
     * 拦截的Controller要以R返回
     */
    @Around("methodPointCut()")
    public R<Object> interceptor(ProceedingJoinPoint pjp){
        R<Object> result=null;
        MethodSignature signature = (MethodSignature)pjp.getSignature();
        RepeatSubmit annotation = signature.getMethod().getAnnotation(RepeatSubmit.class);
        String prefix = annotation.prefix();
        int expire = annotation.expire();//超时时间，重复提交时间间隔
        TimeUnit timeUnit = annotation.timeUnit();

        String key = getKey(prefix);
        String value = LocalDateTime.now().toString();
        try {
            final boolean success = redisLock.lock(key,value, timeUnit, expire);
            if (!success){
                log.error("请勿重复提交");
                result = new R<>();
                result.setCode(1);
                result.setMsg("请勿重复提交");
            } else {
                result=(R<Object>) pjp.proceed();
            }
        }   catch (BusinessException bexception) {
            result = new R<>();
            result.setCode(bexception.getCode());
            result.setMsg(bexception.getMessage());
            bexception.printStackTrace();
            throw new BusinessException(bexception.getCode(),bexception.getMessage());
        }catch (Throwable throwable) {
            result = new R<>();
            result.setCode(1);
            result.setMsg("服务器异常，请联系管理员");
            throwable.printStackTrace();
            throw new BusinessException("服务器异常，请联系管理员");
        } finally {
            redisLock.unlock(key,value);
        }
        return result;
    }

    public static String getKey(String prefix){
        StringBuffer buffer = new StringBuffer();
        // 获取请求URL
        ServletRequestAttributes rs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = rs.getRequest();
        String servletPath = request.getServletPath();
        buffer.append(servletPath);
        String accessToken = UserInfoUtil.getUserInfo().getAccessToken();
        return prefix+accessToken;
    }
}
