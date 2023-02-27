package com.kyxs.cloud.core.base.filter;

import com.kyxs.cloud.core.base.constants.BaseConstants;
import com.kyxs.cloud.core.base.entity.UserInfo;
import com.kyxs.cloud.core.base.utils.UserInfoUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 租户上下文过滤器
 *
 * @author wangsf
 * @date 2022-11-15 13:28:00
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TenantContextHolderFilter extends GenericFilterBean {
    @Override
    @SneakyThrows
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            long tenantId = BaseConstants.TENANT_ID_DEFAULT;
            String authorization = request.getHeader(BaseConstants.REQUEST_HEADER_AUTHORIZATION);
            String token = StringUtils.substringAfter(authorization, BaseConstants.TOKEN_SPLIT);
            if(StringUtils.isNotBlank(token)){
                UserInfo userInfo = UserInfoUtil.getUserInfo(token);
                if(userInfo!=null){
                    tenantId = userInfo.getTenantId();
                }
            }
            log.info("获取到的租户ID为:{}", tenantId);
            TenantContextHolder.setTenantId(tenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContextHolder.clear();
        }
    }
}
