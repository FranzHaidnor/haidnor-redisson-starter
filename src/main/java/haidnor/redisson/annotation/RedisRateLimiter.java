package haidnor.redisson.annotation;

import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;

import java.lang.annotation.*;

/**
 * Redis 限流器
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisRateLimiter {

    /**
     * 限流器名称
     */
    String name();

    /**
     * rate mode
     */
    RateType mode();

    /**
     * 单位时间内的限流次数
     */
    long rate();

    /**
     * 速率时间间隔
     */
    long rateInterval();

    /**
     * 速率时间间隔单位
     */
    RateIntervalUnit rateIntervalUnit();

}
