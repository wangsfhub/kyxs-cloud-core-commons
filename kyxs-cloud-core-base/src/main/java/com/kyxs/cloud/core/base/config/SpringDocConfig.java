package com.kyxs.cloud.core.base.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc API文档相关配置
 * Created by wangsf on 2023/2/25.
 */
@Configuration
public class SpringDocConfig {
    private static final String SECURITY_SCHEME_NAME = "Authorization";
    @Bean
    public OpenAPI mallTinyOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("KYXS-CLOUD API")
                .description("昆云薪事API接口文档")
                .version("v1.0.0")
                .license(new License().name("github地址").url("https://github.com/wangsfhub/kyxs-cloud-front")))
                .externalDocs(new ExternalDocumentation() .description("昆云薪事").url("http://127.0.0.1:7900/swagger-ui/"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components() .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme() .name(SECURITY_SCHEME_NAME) .type(SecurityScheme.Type.HTTP) .scheme("bearer") .bearerFormat("JWT")));
    }
}
