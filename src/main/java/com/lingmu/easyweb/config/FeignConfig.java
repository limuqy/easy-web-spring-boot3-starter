package com.lingmu.easyweb.config;

import com.lingmu.easyweb.core.util.ServletUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class FeignConfig implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Map<String, String> headers = ServletUtil.getHeaders();
        // 传递请求头
        headers.forEach(requestTemplate::header);
    }
}
