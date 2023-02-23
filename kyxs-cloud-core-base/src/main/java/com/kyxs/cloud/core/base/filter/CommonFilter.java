package com.kyxs.cloud.core.base.filter;
import com.kyxs.cloud.core.base.utils.RequestUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class
CommonFilter implements Filter {
    public CommonFilter() {
    }

    public void doFilter(ServletRequest request, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        RequestUtil.setHttpServletRequest((HttpServletRequest)request);
        filterChain.doFilter(request, servletResponse);
        RequestUtil.clean();
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }
}

