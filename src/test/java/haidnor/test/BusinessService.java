package haidnor.test;

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
