package haidnor.redisson.aop;

import haidnor.redisson.annotation.RedisRateLimiter;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * redisson 限流器的切面
 */
@Component
@Aspect
public class RedisRateLimiterAspect {

    private final Set<String> rateLimiterSet = new CopyOnWriteArraySet<>();
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 在指定接口前进行限流
     */
    @Before("@annotation(redisRateLimiter)")
    public void before(RedisRateLimiter redisRateLimiter) {
        String name = redisRateLimiter.name();
        RateType mode = redisRateLimiter.mode();
        long rate = redisRateLimiter.rate();
        long rateInterval = redisRateLimiter.rateInterval();
        RateIntervalUnit rateIntervalUnit = redisRateLimiter.rateIntervalUnit();

        RRateLimiter rateLimiter = redissonClient.getRateLimiter(name);
        if (!rateLimiterSet.contains(name)) {
            rateLimiter.setRate(mode, rate, rateInterval, rateIntervalUnit);
            rateLimiterSet.add(name);
        }

        boolean tryAcquire = rateLimiter.tryAcquire(1);
        if (!tryAcquire) {
            throw new RuntimeException("Access limit exception");
        }
    }

}