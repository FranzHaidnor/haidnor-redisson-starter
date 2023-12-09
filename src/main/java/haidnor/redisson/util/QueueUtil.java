package haidnor.redisson.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QueueUtil {

    /**
     * 延迟消息队列与普通消息队列名称的前缀. 此配置用于区分不同的运行环境
     */
    protected static String queuePrefix;

    /**
     * 修饰队列名称
     *
     * @param queueName 原本的消息队列名称
     */
    public static String modifyQueueName(String queueName) {
        if (queuePrefix.isEmpty()) {
            return queueName;
        }
        return queuePrefix + "_" + queueName;
    }

    @Value("${redisson.queuePrefix:}")
    public void setQueuePrefix(String queuePrefix) {
        QueueUtil.queuePrefix = queuePrefix;
    }

}
