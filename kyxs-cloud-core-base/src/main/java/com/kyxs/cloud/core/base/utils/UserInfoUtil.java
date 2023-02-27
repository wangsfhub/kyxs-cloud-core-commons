package com.kyxs.cloud.core.base.utils;

import com.kyxs.cloud.core.base.entity.UserInfo;
import com.kyxs.cloud.core.base.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 获取用户信息工具类
 */
public class UserInfoUtil {
    //认证参数
    private static final String AUTHORIZATION_VAR = "Authorization";

    public static UserInfo getUserInfo() {
        /**
         * 先写死返回，后续登录完成则完成
         */
        if("admin-accessToken".equals(getAuthorizationToken())){
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(1L);
            userInfo.setUserName("admin");
            userInfo.setCusId(1L);
            userInfo.setTenantId(1L);
            userInfo.setAccessToken("xxxxxxxxxxx");
            userInfo.setPhone("13988430117");
            userInfo.setPhone("13988430117@163.com");
            return userInfo;
        }
        throw new BusinessException(-1, "请登录！");
    }
    public static String getAuthorizationToken() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String token = request.getHeader(AUTHORIZATION_VAR);
        if (token != null && token.equals("Bearer")) {
            token = null;
        } else if (token != null && token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "");
        }
        return StringUtils.isNotBlank(token) ? token : null;
    }
}
