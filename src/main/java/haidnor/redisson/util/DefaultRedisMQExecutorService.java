package haidnor.redisson.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class DefaultRedisMQExecutorService {

    private static final Logger log = LoggerFactory.getLogger(DefaultRedisMQExecutorService.class);

    private static volatile ExecutorService executorService;

    public static ThreadPoolExecutor get() {
        if (executorService == null) {
            synchronized (DefaultRedisMQExecutorService.class) {
                if (executorService == null) {
                    log.debug("initialize DefaultRedisMQExecutor");
                    executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
                }
            }
        }
        return (ThreadPoolExecutor) executorService;
    }

}
