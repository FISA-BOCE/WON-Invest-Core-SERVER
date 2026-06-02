package com.woorifisa.won_invest_core_server.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String JWT_SECURITY_SCHEME = "JWT_TOKEN";

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("INVEST-CORE API 명세서")
                .description("WON해요 증권 계정계 Swagger UI입니다.")
                .version("0.0.1");

        SecurityRequirement jwtSecurityRequirement = new SecurityRequirement()
                .addList(JWT_SECURITY_SCHEME);

        Components components = new Components()
                .addSecuritySchemes(JWT_SECURITY_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        return new OpenAPI()
                .info(info)
                .addServersItem(new Server().url("/"))
                .addSecurityItem(jwtSecurityRequirement)
                .components(components);
    }
}
