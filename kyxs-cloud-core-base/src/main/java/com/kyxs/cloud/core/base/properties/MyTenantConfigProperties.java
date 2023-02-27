package com.kyxs.cloud.core.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 多租户配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "my.cloud.tenant")
public class MyTenantConfigProperties {
    /**
     * 是否开启租户模式
     */
    private Boolean enable = true;

    /**
     * 维护租户列名称
     */
    private String column = "tenant_id";

    /**
     * 多租户的数据表集合
     */
    private List<String> tables = new ArrayList<>();

}
