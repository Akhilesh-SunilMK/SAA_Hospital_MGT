package com.hms.webui.config;

import com.hms.webui.security.AuthInterceptor;
import com.hms.webui.security.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    public MvcConfig(AuthInterceptor authInterceptor, CurrentUserArgumentResolver currentUserArgumentResolver) {
        this.authInterceptor = authInterceptor;
        this.currentUserArgumentResolver = currentUserArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }
}
