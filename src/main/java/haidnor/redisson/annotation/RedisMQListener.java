package haidnor.redisson.annotation;

import java.lang.annotation.*;

/**
 * Redis 普通消息队列监听器
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisMQListener {

    /**
     * The destination name for this listener, resolved through the container-wide
     */
    String destination();

    /**
     * 表示监听消息队列的线程数. 此参数值不可小于 1
     */
    int listenerNum() default 1;

    /**
     * 消息队列消费者使用的线程池
     */
    String executorService() default "defaultRedisMQExecutorService";

}
