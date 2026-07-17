package io.github.limuqy.easyweb.config;

import io.github.limuqy.easyweb.core.thread.EasyThreadFactory;
import io.github.limuqy.easyweb.core.util.ThreadUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Boot3 自动配置——注册虚拟线程工厂，覆盖 core 的默认平台线程工厂。
 *
 * @author limuqy
 */
@Configuration
public class ThreadFactoryConfiguration {

    @Bean
    @ConditionalOnMissingBean(EasyThreadFactory.class)
    public EasyThreadFactory easyThreadFactory() {
        VirtualEasyThreadFactory factory = new VirtualEasyThreadFactory();
        ThreadUtil.setFactory(factory);
        return factory;
    }

}
