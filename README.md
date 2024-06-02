# redisson-spring-boot-starter
Java Redis 客户端。基于 Redisson 再次封装，提供延迟消息队列、消息队列、限流器、分布式锁等易用功能。

# 配置
## pom.xml
```xml
<dependency>
    <groupId>haidnor</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.2.0</version>
</dependency>
```

## yaml
redis 单机配置
```yaml
spring:
  redis:
    database: 0
    port: 6379
    host: 127.0.0.1
    password: root
```
redis 集群配置
```yaml
spring:
  redis:
    sentinel:
      master: mymaster
      nodes: 192.168.12.198:26379,192.168.12.198:26378,192.168.12.198:26377
```

# 分布式锁
简化 Redisson 分布式锁 API 使用方式
```java
import haidnor.redisson.core.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class BusinessService {

    @Autowired
    private RedisLock redisLock;

    /**
     * 上锁代码无返回值, 无获取锁等待时间
     */
    public void demo1() {
        redisLock.lock("LOCK_KEY", () -> {
            // 此处省略业务代码......
        });
    }

    /**
     * 上锁代码无返回值, 有获取锁等待时间
     */
    public void demo2() {
        redisLock.lock("LOCK_KEY", 1, TimeUnit.SECONDS, () -> {
            // 此处省略业务代码......
        });
    }

    /**
     * 上锁代码有返回值, 无获取锁等待时间
     */
    public void demo3() {
        String result = redisLock.lock("LOCK_KEY", () -> {
            // 此处省略业务代码......
            return "返回值";
        });
        System.out.println(result);
    }

    /**
     * 上锁代码有返回值, 有取锁等待时间
     */
    public void demo4() {
        String result = redisLock.lock("LOCK_KEY", 1, TimeUnit.SECONDS, () -> {
            // 此处省略业务代码......
            return "返回值";
        });
        System.out.println(result);
    }
    
}
```

# 消息队列
以下代码示例展示两种消息队列模式,"普通消息队列"和"延迟消息队列"  

**消息队列生产者代码示例**
```java
import haidnor.redisson.core.RedisMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 消息队列消息生产者
 */
@Service
public class MQProducer {

    @Autowired
    private RedisMQService redisMQService;

    /**
     * 向 redis 消息队列中发送消息
     */
    public void sendMes() {
        // Message 可以是其它自定义对象
        Message msg = new Message("ID_01", "这是一条普通消息");
        
        redisMQService.send("general_msg_queue", msg);
    }

    /**
     * 向 redis 消息队列中发送延迟消息. 指定消费者在一定时间后接收到消息
     */
    public void sendDelayMsg() {
        // Message 可以是其它自定义对象
        Message msg = new Message("ID_02", "这是一条延迟消息");
        
        // 发送延迟消息, 可指定任意时间后消费
        redisMQService.send("delay_msg_queue", msg, 1, TimeUnit.SECONDS);
    }
    
}
```
**消息队列消费者示例**

```java
import haidnor.redisson.annotation.RedisDMQHandler;
import haidnor.redisson.annotation.RedisMQListener;
import haidnor.redisson.annotation.RedisMQHandler;
import org.springframework.stereotype.Service;

/**
 * Redis 消息队列消费者
 *
 * 添加 @RedisMQConfiguration 注解标记此类为 Redis 消息队列监听者
 */
@RedisMQListener
@Service
public class MQConsumer {

    /**
     * 普通消息队列消息监听器
     *
     * 参数类型 Message 可以是其它自定义对象, 需要和投递的消息类型保持一致
     */
    @RedisMQHandler(destination = "general_msg_queue")
    public void msgConsumer(Message msg) {
        System.out.println(msg);
    }

    /**
     * 延迟消息队列消息监听器
     *
     * 参数类型 Message 可以是其它自定义对象, 需要和投递的消息类型保持一致
     */
    @RedisDMQHandler(destination = "delay_msg_queue")
    public void delayMsgConsumer(Message msg) {
        System.out.println(msg);
    }

}
```
注意:   
1. 编写消息队列消费者的类必须加上 `@RedisMQListener` 注解后才能监听消息生效。原理是 SpringBoot 启动后会扫描含有其注解标记的类，为此类自动生成代理对象。
2. 普通消息队列监听方法需要标记注解 `@RedisMQHandler`, 延迟消息队列监听方法需要标记注解 `@RedisDMQHandler`。两者请勿混淆使用。

**消息队消费者的高级使用方式**  
`@RedisMQHandler`和`@RedisDMQHandler` 共有三个参数可填
1. `destination` 消息队列名称(必填)
2. `listenerNum` 监听消息队列的线程数，默认值 “1”。 复写此参数值不可小于 1
3. `executorService` 消费者使用的线程池 (Spring Bean 的名称)，默认值 “defaultRedisMQExecutorService”。

**参数 destination 说明**  
默认情况下,在一个服务下定义一个消息队列的监听者只会创建一个队列监听线程， 一般情况下使用使用默认值 1 即可，不需要开发者再自定义设置。  
如果是延迟队列消费者，同一个队列有大量的延迟消息，可以适当提高此参数数值以降低延迟时间误差。（具体数值需要根据测试情况调整）

**参数 executorService 说明**  
默认情况下，消费者使用的是默认的 `defaultRedisMQExecutorService` 线程池进行消费消费的，此线程池的线程数为 （CPU 核心数 * 2），因此是多线程消费的。  
注意: 消息消费者不会使用线程池的阻塞队列存储消息, 当所有核心线程在运行的时候就不会尝试从 Redis 获取消息。**因此最大并发消费线程数只等于线程池的核心线程数量**。

如果您需要为消费者自定义线程池，以下代码可以作为参考：  

定义一个单线程线程池，bean 的名称为 `singletonThreadExecutor`
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class RedisMQExecutorServiceConfig {

    @Bean
    public ExecutorService singletonThreadExecutor() {
        return Executors.newFixedThreadPool(1);
    }

}
```
让消费者使用此线程池

```java
import haidnor.redisson.annotation.RedisDMQHandler;
import haidnor.redisson.annotation.RedisDMQListener;
import haidnor.redisson.annotation.RedisMQListener;
import haidnor.redisson.annotation.RedisMQHandler;
import org.springframework.stereotype.Service;

@RedisMQListener
@Service
public class MQConsumer {

    /**
     * 普通消息队列消息监听器
     */
    @RedisMQHandler(destination = "general_msg_queue", executorService = "singletonThreadExecutor")
    public void msgConsumer(Message msg) {
        System.out.println(msg);
    }

    /**
     * 延迟消息队列消息监听器
     */
    @RedisDMQHandler(destination = "delay_msg_queue", executorService = "singletonThreadExecutor")
    public void delayMsgConsumer(Message msg) {
        System.out.println(msg);
    }

}
```