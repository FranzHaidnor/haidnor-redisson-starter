package haidnor.test;

import haidnor.redisson.annotation.RedisDMQHandler;
import haidnor.redisson.annotation.RedisMQListener;
import haidnor.redisson.annotation.RedisMQHandler;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Redis 消息队列消费者
 * <p>
 * 添加 @RedisMQConfiguration 注解标记此类为 Redis 消息队列监听者
 */
@RedisMQListener
@Service
public class MQConsumer {

    /**
     * 普通消息队列消息监听器
     */
    @RedisMQHandler(destination = "general_msg_queue")
    public void msgConsumer(Message msg) {
        System.out.println(msg);
    }

    /**
     * 延迟消息队列消息监听器
     */
    @RedisDMQHandler(destination = "delay_msg_queue")
    public void delayMsgConsumer(Message msg) {
        System.out.println(msg);
    }

    /**
     * 延迟消息队列消息监听器
     */
    @RedisDMQHandler(destination = "test_queue")
    public void delayMsgConsumer1(Map msg) {
        System.out.println(msg);
    }

}
