package io.github.limuqy.easyweb.config;

import io.github.limuqy.easyweb.core.context.AppContext;
import io.github.limuqy.easyweb.core.queue.PutBlockingQueue;
import io.github.limuqy.easyweb.core.thread.DefaultEasyThreadFactory;
import io.github.limuqy.easyweb.core.thread.EasyThreadFactory;
import io.github.limuqy.easyweb.model.core.UserProfile;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 虚拟线程工厂，使用 Java 21 虚拟线程实现 {@link EasyThreadFactory}。
 * <p>
 * 上下文传播逻辑与 {@link DefaultEasyThreadFactory} 一致，仅线程类型替换为虚拟线程。
 *
 * @author limuqy
 */
public class VirtualEasyThreadFactory implements EasyThreadFactory {

    private final ThreadFactory virtualFactory = Thread.ofVirtual().factory();

    @Override
    public void execute(Runnable task) {
        virtualFactory.newThread(wrap(task)).start();
    }

    @Override
    public ExecutorService newExecutorService(int corePoolSize, int maxQueue) {
        return new ThreadPoolExecutor(
                corePoolSize, corePoolSize,
                5L, TimeUnit.MINUTES,
                new PutBlockingQueue<>(maxQueue),
                virtualFactory);
    }

    // ── 上下文传播 ──────────────────────────────────────────

    Runnable wrap(final Runnable task) {
        UserProfile userProfile = AppContext.getUserProfile();
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            DefaultEasyThreadFactory.restoreContext(contextMap, userProfile);
            try {
                task.run();
            } finally {
                DefaultEasyThreadFactory.clearContext();
            }
        };
    }
}
