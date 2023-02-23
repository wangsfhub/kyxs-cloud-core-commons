package com.kyxs.cloud.core.base.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BusinessException extends RuntimeException {
    public static final Integer RUNTIME_EXCEPTION_CODE = 1;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Integer code, String message) {
        super(code + "||" + message);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BusinessException(ICodeMessage codeMessage) {
        super(codeMessage.getCode() + "||" + codeMessage.getMessage());
    }

    public Integer getCode() {
        return RUNTIME_EXCEPTION_CODE;
    }

    public BusinessException() {
    }

}
