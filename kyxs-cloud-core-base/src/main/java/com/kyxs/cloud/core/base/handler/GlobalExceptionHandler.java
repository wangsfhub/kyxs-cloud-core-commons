package com.kyxs.cloud.core.base.handler;

import com.kyxs.cloud.core.base.exception.BusinessException;
import com.kyxs.cloud.core.base.result.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 自定义全局异常处理器
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ResponseBody
    @ExceptionHandler(BusinessException.class)
    public R handleBusinessException(BusinessException e){
        log.error(e.getMessage(),e);
        R<String> r = new R();
        String message = e.getMessage();
        r.setCode(e.getCode());
        r.setMsg(e.getMessage());
        if(StringUtils.isNotEmpty(message)&&message.contains("||")){
            try {
                int code =Integer.parseInt(message.substring(0,message.indexOf("||")));
                r.setCode(code);
                r.setMsg(message.substring(message.indexOf("||")+2));
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return r;
    }
    /**
     * 自定义验证异常
     */
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return R.failed(message);
    }
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public R handleException(Exception e){
        log.error(e.getMessage(),e);
        R<String> r = new R();
        r.setCode(1);
        r.setMsg("服务器异常，请联系管理");
        return r;

    }
}
