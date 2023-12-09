package haidnor.redisson.core;

import haidnor.redisson.util.QueueUtil;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redisson 队列工具类
 * <p>
 * <a href="https://github.com/redisson/redisson/wiki/7.-distributed-collections/#715-delayed-queue">...</a>
 */
@Service
public class RedisMQService {

    @Autowired
    private RedissonClient redisson;

    /**
     * 发送普通消息到消息队列
     *
     * @param queueName 队列名称
     * @param msg       任务对象
     */
    public <T> boolean send(String queueName, T msg) {
        String destination = QueueUtil.modifyQueueName(queueName);
        RBlockingQueue<T> blockingFairQueue = redisson.getBlockingQueue(destination, JsonJacksonCodec.INSTANCE);
        return blockingFairQueue.offer(msg);
    }

    /**
     * 发送延迟消息到消息队列
     * <p>
     * <a href="https://github.com/redisson/redisson/wiki/7.-distributed-collections#715-delayed-queue">...</a>
     *
     * @param queueName 队列名称
     * @param msg       任务对象
     * @param delayTime 消息消费延迟时间
     * @param timeUnit  时间单位
     */
    public <T> void send(String queueName, T msg, long delayTime, TimeUnit timeUnit) {
        String destination = QueueUtil.modifyQueueName(queueName);
        RBlockingQueue<T> blockingFairQueue = redisson.getBlockingQueue(destination, JsonJacksonCodec.INSTANCE);
        RDelayedQueue<T> delayedQueue = redisson.getDelayedQueue(blockingFairQueue);
        delayedQueue.offer(msg, delayTime, timeUnit);
        delayedQueue.destroy();
    }

}
