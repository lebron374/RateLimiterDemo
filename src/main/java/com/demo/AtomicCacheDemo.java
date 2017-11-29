package com.demo;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhi.wang on 2017/11/28.
 */
public class AtomicCacheDemo {
    private static final AtomicInteger tag = new AtomicInteger(5000);
    private static final int DEFAULT_CONCURRENCY_LEVEL = 32;
    private static final long DEFAULT_EXPIRE_TIME = 12;
    private static Map<String, Cache<String, Object>> localCachePool = new ConcurrentHashMap<String, Cache<String, Object>>();

    static {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        tag.set(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private static Cache<String, Object> getCache(String resourceId) {
        Cache<String, Object> localCache = localCachePool.get(resourceId);
        if (null != localCache) {
            return localCache;
        }

        localCache = CacheBuilder.newBuilder().concurrencyLevel(DEFAULT_CONCURRENCY_LEVEL)
                .expireAfterAccess(DEFAULT_EXPIRE_TIME, TimeUnit.HOURS)
                .recordStats()
                .build();
        localCachePool.put(resourceId, localCache);

        return localCache;
    }

    private static boolean tryAcquire() {
        if (tag.decrementAndGet() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i=0; i<10; i++) {
            final int j = i;
            executorService.submit(new Runnable() {
                public void run() {
                    for (int k=0; k<20000; k++) {
                        Cache<String, Object> cache = getCache(String.valueOf(j));
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < 5000; i++) {
                            if (tryAcquire()) {
                                cache.put(String.valueOf(i), i);
                            }
                        }
                        long end = System.currentTimeMillis();
                        if (true) {
                            System.out.println((end - start));
                        }
                    }
                }
            });
        }
    }
}
