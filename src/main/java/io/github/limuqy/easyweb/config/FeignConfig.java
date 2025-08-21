package io.github.limuqy.easyweb.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.github.limuqy.easyweb.core.constant.Constant;
import io.github.limuqy.easyweb.core.context.AppContext;
import io.github.limuqy.easyweb.core.util.StringUtil;
import io.github.limuqy.easyweb.core.util.TraceIdUtil;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        // 传递请求头
        if (StringUtil.isNoneBlank(TraceIdUtil.getTraceId())) {
            requestTemplate.header(TraceIdUtil.TRACE_ID, TraceIdUtil.getTraceId());
        }
        if (!AppContext.isAnonymous() && !requestTemplate.headers().containsKey(Constant.HEADER_TOKEN)) {
            requestTemplate.header(Constant.HEADER_TOKEN, AppContext.getUserProfile().getToken());
        }
    }
}
