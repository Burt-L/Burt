package com.burt.bucket;

import com.google.common.util.concurrent.RateLimiter;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 限流之令牌桶demo
 * Created by Burt on 2017/3/5 0005.
 */
public class TokenBucket {

    private static final ConcurrentHashMap<String, RateLimiter> resourceRateLimiterMap = new ConcurrentHashMap<String, RateLimiter>();

    /**
     * 创建令牌
     * @param resouce  map的key
     * @param qps qps
     */
    public static void createFlowLimitMap(String resouce, double qps) {
        RateLimiter limiter = resourceRateLimiterMap.get(resouce);
        if (null == limiter) {
            limiter = RateLimiter.create(qps);
            resourceRateLimiterMap.putIfAbsent(resouce, limiter);
        }
        limiter.setRate(qps);
    }

    public static boolean grant(String resouce) throws Exception {
        RateLimiter limiter = resourceRateLimiterMap.get(resouce);
        if (null == limiter) {
            throw new Exception(resouce);
        }
        //请求获取令牌
        if (!limiter.tryAcquire()) {
//            System.out.println("----令牌不够了");
            return false;
        }
        return true;
    }

    static class TestWork implements Runnable {

        public void run() {
            try {
                if (grant("burt")) {
                    System.out.println("+++++++执行业务逻辑");
                } else {
                    System.out.println("--------被限流");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String souce = "burt";
        double qps = 10;
        createFlowLimitMap(souce, qps);

        //创建线程池
        ExecutorService pools = Executors.newFixedThreadPool(20);
        TokenBucket.TestWork testWork = new TokenBucket.TestWork();
        for (int i = 0; i < 200; i++) {
            Random random = new Random();
            int time = random.nextInt(100);
            Thread.sleep(time);
            pools.submit(testWork);
        }
    }
}
