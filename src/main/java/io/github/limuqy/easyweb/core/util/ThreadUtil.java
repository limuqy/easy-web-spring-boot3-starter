package io.github.limuqy.easyweb.core.util;

import io.github.limuqy.easyweb.core.context.AppContext;
import io.github.limuqy.easyweb.model.core.UserProfile;
import lombok.NonNull;

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
        return () -> {
            AppContext.setUserProfile(userProfile);
            return callable.call();
        };
    }

    public static Runnable wrap(final Runnable runnable) {
        UserProfile userProfile = AppContext.getUserProfile();
        return () -> {
            AppContext.setUserProfile(userProfile);
            runnable.run();
        };
    }

    public static ExecutorService blockingVirtualService(int corePoolSize, int maxQueue) {
        return new ThreadPoolExecutor(1, corePoolSize, 1L, TimeUnit.MINUTES, new PutBlockingQueue<>(maxQueue), Thread.ofVirtual().factory());
    }

    /**
     * 重写offer为阻塞操作
     */
    private static class PutBlockingQueue<T> extends LinkedBlockingQueue<T> {

        public PutBlockingQueue(int size) {
            super(size);
        }

        @Override
        public boolean offer(@NonNull T t) {
            try {
                put(t);
                return true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}
