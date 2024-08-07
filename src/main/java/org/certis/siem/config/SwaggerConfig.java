package org.certis.siem.config;


import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi OpenApi() {
        String[] paths = { "/**" };
        return GroupedOpenApi.builder().
                group("siem")
                .addOpenApiCustomizer(openApi -> openApi.info(new Info().title("System API")))
                .pathsToMatch(paths)
                .build();
    }
}

