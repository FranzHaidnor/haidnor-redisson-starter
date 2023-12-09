package haidnor.test;

import haidnor.redisson.annotation.RedisDMQListener;
import haidnor.redisson.annotation.RedisMQConfiguration;
import haidnor.redisson.annotation.RedisMQListener;
import org.springframework.stereotype.Service;

/**
 * Redis 消息队列消费者
 * <p>
 * 添加 @RedisMQConfiguration 注解标记此类为 Redis 消息队列监听者
 */
@RedisMQConfiguration
@Service
public class MQConsumer {

    /**
     * 普通消息队列消息监听器
     */
    @RedisMQListener(destination = "general_msg_queue")
    public void msgConsumer(Message msg) {
        System.out.println(msg);
    }

    /**
     * 延迟消息队列消息监听器
     */
    @RedisDMQListener(destination = "delay_msg_queue")
    public void delayMsgConsumer(Message msg) {
        System.out.println(msg);
    }

}
