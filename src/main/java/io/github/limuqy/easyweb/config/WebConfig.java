package io.github.limuqy.easyweb.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedOrigins("*")
                .allowCredentials(false);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.addFirst(new RowIdArgumentResolver());
    }

    /**
     * 添加日志traceId过滤器
     */
    @Bean
    public FilterRegistrationBean<EasyWebFilter> traceIdFilter() {
        FilterRegistrationBean<EasyWebFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new EasyWebFilter());
        registration.addUrlPatterns("/*");
        registration.setName("easyWebFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
