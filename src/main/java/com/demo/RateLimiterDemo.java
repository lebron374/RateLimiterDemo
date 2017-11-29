package com.demo;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhi.wang on 2017/11/28.
 */
public class RateLimiterDemo {
    private static final int RATE_LIMIT = 1000000;
    private static final int DEFAULT_CONCURRENCY_LEVEL = 32;
    private static final long DEFAULT_EXPIRE_TIME = 12;
    private static Map<String, Cache<String, Object>> localCachePool = new ConcurrentHashMap<String, Cache<String, Object>>();
    private static final RateLimiter rateLimiter = RateLimiter.create(RATE_LIMIT);

    public static boolean tryAcquire() {
        if (rateLimiter.tryAcquire()) {
            return true;
        } else {
            return false;
        }
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

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i=0; i<10; i++) {
            final int j = i;
            final Map<Integer,Integer> map = new ConcurrentHashMap<Integer, Integer>();
            executorService.submit(new Runnable() {
                public void run() {
                    for (int k=0; k<1; k++){
                        Cache<String, Object> cache = getCache(String.valueOf(j));
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < 100000; i++) {
                            if (true) {
                                cache.put(String.valueOf(i), i);
                            }
                        }
                        long end = System.currentTimeMillis();
                        if (true) {
//                            System.out.println((end - start));
//                            System.out.println(cache.size());
                            System.out.println((end-start)*1000000/cache.size());
                        }
                    }
                }
            });
        }
    }
}
