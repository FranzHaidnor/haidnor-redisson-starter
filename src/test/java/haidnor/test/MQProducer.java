package haidnor.test;

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
        // Message 可以是别的自定义对象
        Message msg = new Message("ID_01", "这是一条普通消息");

        redisMQService.send("general_msg_queue", msg);
    }

    /**
     * 向 redis 消息队列中发送延迟消息. 指定消费者在一定时间后接收到消息
     */
    public void sendDelayMsg() {
        // Message 可以是别的自定义对象
        Message msg = new Message("ID_02", "这是一条延迟消息");

        // 发送延迟消息, 可指定任意时间后消费
        redisMQService.send("delay_msg_queue", msg, 1, TimeUnit.SECONDS);
    }

}
