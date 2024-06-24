package org.certis.siem.config;


import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi SystemOpenApi() {
        String[] paths = { "/system/**" };
        return GroupedOpenApi.builder().
                group("system")
                .addOpenApiCustomizer(openApi -> openApi.info(new Info().title("System API")))
                .pathsToMatch(paths)
                .build();
    }
    @Bean
    public GroupedOpenApi elasticOpenApi() {
        String[] paths = { "/elastic-cluster/**" };
        return GroupedOpenApi.builder().
                group("elasticsearch")
                .addOpenApiCustomizer(openApi -> openApi.info(new Info().title("Elasticsearch API")))
                .pathsToMatch(paths)
                .build();
    }

    @Bean
    public GroupedOpenApi cloudTrailOpenApi() {
        String[] paths = { "/cloudTrail/**" };
        return GroupedOpenApi.builder().
                group("cloudTrail")
                .addOpenApiCustomizer(openApi -> openApi.info(new Info().title("cloudTrail API")))
                .pathsToMatch(paths)
                .build();
    }
}

