package com.kyxs.cloud.core.base.filter;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.experimental.UtilityClass;

/**
 * 多租户上下文
 *
 * @author wangsf
 * @since 2022-12-3
 */
@UtilityClass
public class TenantContextHolder {

    /**
     * 支持父子线程数据传递
     */
    private final ThreadLocal<Long> THREAD_LOCAL_TENANT = new TransmittableThreadLocal<>();

    /**
     * 设置租户ID
     *
     * @param tenantId 租户ID
     */
    public void setTenantId(Long tenantId) {
        THREAD_LOCAL_TENANT.set(tenantId);
    }

    /**
     * 获取租户ID
     *
     * @return String
     */
    public Long getTenantId() {
        return THREAD_LOCAL_TENANT.get();
    }

    /**
     * 清除tenantId
     */
    public void clear() {
        THREAD_LOCAL_TENANT.remove();
    }
}