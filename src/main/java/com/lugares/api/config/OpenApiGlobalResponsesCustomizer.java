package com.lugares.api.config;

import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiGlobalResponsesCustomizer {

    @Bean
    public OpenApiCustomizer globalErrorResponses() {
        return openApi -> {
            ApiResponse r401 = new ApiResponse().description("Unauthorized - missing or invalid JWT");
            ApiResponse r403 = new ApiResponse().description("Forbidden - insufficient role or not owner of the resource");
            ApiResponse r500 = new ApiResponse().description("Internal server error");
            openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(op -> {
                    op.getResponses().addApiResponse("401", r401);
                    op.getResponses().addApiResponse("403", r403);
                    op.getResponses().addApiResponse("500", r500);
                }));
        };
    }
}
