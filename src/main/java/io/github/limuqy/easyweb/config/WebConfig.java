package io.github.limuqy.easyweb.config;

import io.github.limuqy.easyweb.core.config.CorsProperties;
import io.github.limuqy.easyweb.core.config.EasyWebProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final EasyWebProperties easyWebProperties;

    public WebConfig(EasyWebProperties easyWebProperties) {
        this.easyWebProperties = easyWebProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (easyWebProperties == null || easyWebProperties.getCors() == null) {
            return;
        }
        CorsProperties cors = easyWebProperties.getCors();
        // 仅当显式配置了 allowed-origins 时才启用 CORS
        if (CollectionUtils.isEmpty(cors.getAllowedOrigins())) {
            return;
        }
        registry.addMapping("/**")
                .allowedOrigins(cors.getAllowedOrigins().toArray(new String[0]))
                .allowedHeaders(cors.getAllowedHeaders())
                .allowedMethods(cors.getAllowedMethods())
                .exposedHeaders(cors.getExposedHeaders())
                .allowCredentials(cors.getAllowCredentials() != null && cors.getAllowCredentials())
                .maxAge(cors.getMaxAge());
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
