package com.kyxs.cloud.core.base.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.MybatisMapWrapperFactory;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.kyxs.cloud.core.base.constants.BaseConstants;
import com.kyxs.cloud.core.base.filter.TenantContextHolder;
import com.kyxs.cloud.core.base.interceptor.MyPaginationInnerInterceptor;
import com.kyxs.cloud.core.base.properties.MyTenantConfigProperties;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.kyxs.**.mapper"})
public class MybatisPlusConfig {
    @Autowired
    private MyTenantConfigProperties properties;
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public String getTenantIdColumn() {
                return properties.getColumn();
            }
            @Override
            public Expression getTenantId() {
                long tenantId = TenantContextHolder.getTenantId();
                if (tenantId!=0L) {
                    return new LongValue(tenantId);
                }
                return new NullValue();
            }

            // 这是 default 方法,默认返回 false 表示所有表都需要拼多租户条件
            @Override
            public boolean ignoreTable(String tableName) {
                long tenantId = TenantContextHolder.getTenantId();
                if(properties.getEnable() && tenantId!=BaseConstants.TENANT_ID_DEFAULT){
                    return properties.getTables().contains(tableName.toLowerCase());
                }
                return true;
            }
        }));
        // 如果用了分页插件注意先 add TenantLineInnerInterceptor 再 add PaginationInnerInterceptor
        // 用了分页插件必须设置 MybatisConfiguration#useDeprecatedExecutor = false
        interceptor.addInnerInterceptor(new MyPaginationInnerInterceptor());
        return interceptor;
    }
    //mybatis查询map转驼峰
    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer(){
        System.out.println("initiazing ConfigurationCustomizer....");
        return new ConfigurationCustomizer() {
            @Override
            public void customize(MybatisConfiguration configuration) {
                configuration.setObjectWrapperFactory(new MybatisMapWrapperFactory());
            }
        };
    }

}
