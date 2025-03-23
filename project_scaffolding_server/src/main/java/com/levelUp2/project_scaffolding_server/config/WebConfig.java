package com.levelUp2.project_scaffolding_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.levelUp2.project_scaffolding_server.control.AuthInterceptor;

import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // Add authentication interceptor to verify access token on each request
        registry.addInterceptor(new AuthInterceptor()).addPathPatterns("/scaff/**");
    }
}