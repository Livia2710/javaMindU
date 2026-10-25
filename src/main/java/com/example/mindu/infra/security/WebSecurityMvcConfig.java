package com.example.mindu.infra.security;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
@RequiredArgsConstructor
public class WebSecurityMvcConfig implements WebMvcConfigurer {
    private final LoginRateLimitInterceptor loginRateLimitInterceptor;
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginRateLimitInterceptor).addPathPatterns("/v1/auth/login");
    }
}
