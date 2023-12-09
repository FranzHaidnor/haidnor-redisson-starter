package haidnor.redisson.application;

import haidnor.redisson.annotation.RedisDMQListener;
import haidnor.redisson.annotation.RedisMQConfiguration;
import haidnor.redisson.annotation.RedisMQListener;
import haidnor.redisson.util.DefaultRedisMQExecutorService;
import haidnor.redisson.util.QueueUtil;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

/**
 * Redis 消息队列监听器自动注册
 */
@Component
public class RedisMQListenerStartup implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RedisMQListenerStartup.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedissonClient redisson;


    /**
     * 扫描 RedisMSConfiguration 注解标记的 Bean 类, 发现 RedisDMSListener, RedisMSListener 注解标记的方法, 并将其注册为 Redis MQ 监听器
     */
    @Override
    public void run(ApplicationArguments applicationArguments) {
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(RedisMQConfiguration.class);
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> clazz = bean.getClass();
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                // 注册延迟队列监听器 (使用 AnnotationUtils.findAnnotation(Method method, Class<A> annotationType) 是为了避免 cglib 代理后无法从方法上获取自定义注解)
                RedisDMQListener dmsAnnotation = AnnotationUtils.findAnnotation(method, RedisDMQListener.class);
                if (dmsAnnotation != null) {
                    startDelayedMessageQueueListener(dmsAnnotation.destination(), dmsAnnotation.listenerNum(), dmsAnnotation.executorService(), msg -> {
                        try {
                            method.invoke(bean, msg);
                        } catch (IllegalAccessException | InvocationTargetException exception) {
                            log.error("Redis message queue listener invoke consumer method error", exception);
                        }
                    });
                    log.info("Register redis delayed message queue listener. Class:{} Method:{} Destination:{}", clazz.getName(), method.getName(), QueueUtil.modifyQueueName(dmsAnnotation.destination()));
                }
                // 注册普通消息队列监听器
                RedisMQListener msAnnotation = AnnotationUtils.findAnnotation(method, RedisMQListener.class);
                if (msAnnotation != null) {
                    startMessageQueueListener(msAnnotation.destination(), msAnnotation.listenerNum(), msAnnotation.executorService(), msg -> {
                        try {
                            method.invoke(bean, msg);
                        } catch (IllegalAccessException | InvocationTargetException exception) {
                            log.error("RedisMQ listener invoke consumer method error", exception);
                        }
                    });
                    log.info("Register redis message queue listener. Class:{} Method:{} Destination:{}", clazz.getName(), method.getName(), QueueUtil.modifyQueueName(msAnnotation.destination()));
                }
            }
        }
    }

    /**
     * 开启延迟队列监听器
     *
     * @param queueName       队列名称
     * @param listenerNum     监听器线程数
     * @param executorService 处理消息的线程池 bean 名称
     * @param consumer        消费接口
     */
    private <T> void startDelayedMessageQueueListener(String queueName, int listenerNum, String executorService, Consumer<T> consumer) {
        String destination = QueueUtil.modifyQueueName(queueName);
        if (listenerNum < 1) {
            throw new IllegalArgumentException("the concurrency cannot be less than 1 !");
        }
        ThreadPoolExecutor executor = getExecutorService(executorService);
        Semaphore semaphore = new Semaphore(executor.getCorePoolSize());

        for (int i = 0; i < listenerNum; i++) {
            Thread listener = new Thread(() -> {
                RBlockingQueue<T> blockingFairQueue = redisson.getBlockingQueue(destination, JsonJacksonCodec.INSTANCE);
                redisson.getDelayedQueue(blockingFairQueue);
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        semaphore.acquire();
                        T msg = blockingFairQueue.take();
                        executor.execute(() -> {
                            try {
                                consumer.accept(msg);
                            } catch (Exception exception) {
                                log.error("consume delayed queue exception", exception);
                            } finally {
                                semaphore.release();
                            }
                        });
                    } catch (InterruptedException exception) {
                        log.error("consume delayed queue exception", exception);
                    }
                }
            });
            listener.setName("RedisDelayedMessageQueueListener_" + destination + "_" + i);
            listener.start();
        }
    }


    /**
     * 开启队列监听器
     *
     * @param queueName       队列名称
     * @param listenerNum     监听器线程数
     * @param executorService 处理消息的线程池 bean 名称
     * @param consumer        消费接口
     */
    private <T> void startMessageQueueListener(String queueName, int listenerNum, String executorService, Consumer<T> consumer) {
        String destination = QueueUtil.modifyQueueName(queueName);
        if (listenerNum < 1) {
            throw new IllegalArgumentException("the concurrency cannot be less than 1 !");
        }
        ThreadPoolExecutor executor = getExecutorService(executorService);
        Semaphore semaphore = new Semaphore(executor.getCorePoolSize());

        for (int i = 0; i < listenerNum; i++) {
            Thread listener = new Thread(() -> {
                RBlockingQueue<T> blockingFairQueue = redisson.getBlockingQueue(destination, JsonJacksonCodec.INSTANCE);

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        semaphore.acquire();
                        T task = blockingFairQueue.take();
                        executor.execute(() -> {
                            try {
                                consumer.accept(task);
                            } catch (Exception exception) {
                                log.error("consume queue exception", exception);
                            } finally {
                                semaphore.release();
                            }
                        });
                    } catch (InterruptedException exception) {
                        log.error("consume queue exception", exception);
                    }
                }
            });
            listener.setName("RedissonMessageQueueListener_" + destination + "_" + i);
            listener.start();
        }
    }

    /**
     * 获取消息队列消费者线程池
     *
     * @param executorServiceName 线程池 bean 名称
     */
    private ThreadPoolExecutor getExecutorService(String executorServiceName) {
        if (applicationContext.containsBean(executorServiceName)) {
            return applicationContext.getBean(executorServiceName, ThreadPoolExecutor.class);
        } else {
            return DefaultRedisMQExecutorService.get();
        }
    }

}
