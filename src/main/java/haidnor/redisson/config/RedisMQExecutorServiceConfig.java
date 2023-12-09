package haidnor.redisson.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * redis mq 消费者默认执行线程池
 */
@Configuration
public class RedisMQExecutorServiceConfig {

    @Bean
    @Lazy
    public ExecutorService defaultRedisMQExecutorService() {
        System.out.println("load bean defaultRedisMQExecutorService");
        return Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

}

