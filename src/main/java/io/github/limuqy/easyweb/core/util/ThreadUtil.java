package io.github.limuqy.easyweb.core.util;

import io.github.limuqy.easyweb.core.context.AppContext;
import io.github.limuqy.easyweb.core.queue.PutBlockingQueue;
import io.github.limuqy.easyweb.model.core.UserProfile;

import java.util.Objects;
import java.util.concurrent.*;

public class ThreadUtil {

    public static ExecutorService virtualExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    public static void startVirtualThread(Runnable task) {
        Thread.startVirtualThread(wrap(task));
    }

    public static void startAsync(Runnable task) {
        Thread.startVirtualThread(wrap(task));
    }

    public static void start(Runnable task) {
        try {
            Thread.startVirtualThread(wrap(task)).join();
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    public static <T> Callable<T> wrap(final Callable<T> callable) {
        UserProfile userProfile = AppContext.getUserProfile();
        String traceId = TraceIdUtil.getTraceId();
        return () -> {
            if (StringUtil.isNoneBlank(traceId)) {
                TraceIdUtil.setTraceId(traceId);
            }
            AppContext.setUserProfile(userProfile);
            return callable.call();
        };
    }

    public static Runnable wrap(final Runnable runnable) {
        UserProfile userProfile = AppContext.getUserProfile();
        String traceId = TraceIdUtil.getTraceId();
        return () -> {
            if (StringUtil.isNoneBlank(traceId)) {
                TraceIdUtil.setTraceId(traceId);
            }
            AppContext.setUserProfile(userProfile);
            runnable.run();
        };
    }

    public static ExecutorService blockingVirtualService(int corePoolSize, int maxQueue) {
        return new ThreadPoolExecutor(1, corePoolSize, 1L, TimeUnit.MINUTES, new PutBlockingQueue<>(maxQueue), Thread.ofVirtual().factory());
    }

    public static ExecutorService blockingVirtualService(int maxQueue) {
        return new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors(), 5L, TimeUnit.MINUTES, new PutBlockingQueue<>(maxQueue), Thread.ofVirtual().factory());
    }

    public static void closeExecutor(ExecutorService executorService) {
        if (Objects.isNull(executorService)) {
            return;
        }
        boolean terminated = executorService.isTerminated();
        if (!terminated) {
            executorService.shutdown();
            boolean interrupted = false;
            while (!terminated) {
                try {
                    terminated = executorService.awaitTermination(2L, TimeUnit.HOURS);
                } catch (InterruptedException e) {
                    if (!interrupted) {
                        executorService.shutdownNow();
                        interrupted = true;
                    }
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
