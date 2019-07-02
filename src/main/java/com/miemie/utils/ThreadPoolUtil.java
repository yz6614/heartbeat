package com.miemie.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Xiaomie Wang
 */
public class ThreadPoolUtil {

    private static volatile ThreadPoolUtil INSTANCE;
    private static ExecutorService executorService;

    class DefaultThreadFactory implements ThreadFactory {

        private ThreadGroup threadGroup = null;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private String threadNamepre = null;

        public DefaultThreadFactory(String name) {
            SecurityManager securityManager = System.getSecurityManager();
            threadGroup = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
            threadNamepre = name + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(this.threadGroup, r, threadNamepre + threadNumber.getAndIncrement(), 0);
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            if (thread.getPriority() != Thread.NORM_PRIORITY) {
                thread.setPriority(Thread.NORM_PRIORITY);
            }
            return thread;
        }
    }

    private ThreadPoolUtil() {
        executorService = new ThreadPoolExecutor(10, 20, 10L, TimeUnit.SECONDS
                , new LinkedBlockingQueue<>()
                , new DefaultThreadFactory("heart-beat:"));
    }

    public static ThreadPoolUtil getInstance() {
        if (null == INSTANCE) {
            synchronized (ThreadPoolUtil.class) {
                if (null == INSTANCE) {
                    INSTANCE = new ThreadPoolUtil();
                }
            }
        }
        return INSTANCE;
    }


    /**
     * @return
     */
    public ExecutorService getThreadPoolManager() {
        return executorService;
    }

    /**
     * stop the thread pool
     */
    public void close() {
        executorService.shutdown();
    }
}
