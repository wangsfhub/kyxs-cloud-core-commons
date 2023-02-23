package com.kyxs.cloud.core.base.utils;

import com.alibaba.ttl.TransmittableThreadLocal;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author wangsf
 * @Date 2022/2/20
 * @Version 1.0.0
 */
public final class RequestUtil {
    public static final TransmittableThreadLocal<HttpServletRequest> REQUEST_THREAD_LOCAL = new TransmittableThreadLocal();

    public static HttpServletRequest getHttpServletRequest() {
        return (HttpServletRequest)REQUEST_THREAD_LOCAL.get();
    }

    public static void setHttpServletRequest(HttpServletRequest request) {
        REQUEST_THREAD_LOCAL.set(request);
    }

    public static void clean() {
        REQUEST_THREAD_LOCAL.remove();
    }

    private RequestUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
