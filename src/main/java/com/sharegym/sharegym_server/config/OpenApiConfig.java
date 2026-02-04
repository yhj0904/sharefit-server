package com.sharegym.sharegym_server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.List;

/**
 * OpenAPI (Swagger) 설정
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "bearerAuth";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        // 서버 설정
        List<Server> servers = getServers();

        return new OpenAPI()
                .info(getInfo())
                .servers(servers)
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    private Info getInfo() {
        return new Info()
                .title("ShareFit API")
                .description("ShareFit 피트니스 트래킹 애플리케이션 백엔드 API")
                .version("v1.0.0")
                .contact(new Contact()
                        .name("ShareFit Team")
                        .email("support@garabu.org")
                        .url("https://api.garabu.org/sharefit"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("http://www.apache.org/licenses/LICENSE-2.0"));
    }

    private List<Server> getServers() {
        if ("prod".equals(activeProfile)) {
            // 운영 환경
            return Arrays.asList(
                new Server()
                    .url("https://api.garabu.org/sharefit/api/v1")
                    .description("Production Server"),
                new Server()
                    .url("http://localhost:8080/sharefit/api/v1")
                    .description("Local Test with Production Config")
            );
        } else {
            // 개발 환경
            return Arrays.asList(
                new Server()
                    .url("http://localhost:8080/api/v1")
                    .description("Local Development Server"),
                new Server()
                    .url("https://api.garabu.org/sharefit/api/v1")
                    .description("Production Server (Reference)")
            );
        }
    }
}