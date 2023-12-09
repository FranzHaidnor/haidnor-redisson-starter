package haidnor.redisson.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Redis 标记此类为 Redis MQ Service
 * 告知 RMSListenerStartup 扫描并注册此类下的 RedisMSListener, RedisDMSListener
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RedisMQConfiguration {

}
