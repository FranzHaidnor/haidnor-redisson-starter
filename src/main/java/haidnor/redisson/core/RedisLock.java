package haidnor.redisson.core;

import haidnor.redisson.lambda.Param0Function;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Redisson 锁工具,简化 try catch 代码
 */
@Service
public class RedisLock {

    private final Map<String/*lock key*/, ReentrantLock> reentrantLockMap = new ConcurrentHashMap<>();

    @Autowired
    private RedissonClient redisson;


    /**
     * 上锁并执行代码
     *
     * @param key      分布式锁 key
     * @param time     获取锁等待时间
     * @param timeUnit 获取锁等待时间单位
     * @param supplier 需要执行的代码块
     * @param <T>      生产者返回值泛型
     * @return 执行的代码块返回值
     */
    public <T> T lock(String key, long time, TimeUnit timeUnit, Supplier<T> supplier) {
        long t1 = System.currentTimeMillis();
        return localLock(key, time, timeUnit, () -> {
            RLock lock = redisson.getLock(key);
            try {
                if (lock.tryLock(System.currentTimeMillis() - t1, timeUnit)) {
                    return supplier.get();
                } else {
                    throw new RuntimeException("get redisson lock failed");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("get redisson lock failed");
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        });
    }

    /**
     * 上锁并执行代码
     *
     * @param key      分布式锁 key
     * @param supplier 需要执行的代码块
     * @param <T>      生产者返回值泛型
     * @return 执行的代码块返回值
     */
    public <T> T lock(String key, Supplier<T> supplier) {
        return localLock(key, () -> {
            RLock lock = redisson.getLock(key);
            try {
                if (lock.tryLock()) {
                    return supplier.get();
                } else {
                    throw new RuntimeException("get redisson lock failed");
                }
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        });
    }

    /**
     * 上锁并执行代码
     *
     * @param key      分布式锁 key
     * @param function 需要执行的代码块
     */
    public void lock(String key, Param0Function function) {
        localLock(key, () -> {
            RLock lock = redisson.getLock(key);
            try {
                if (lock.tryLock()) {
                    function.apply();
                } else {
                    throw new RuntimeException("get redisson lock failed");
                }
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        });
    }

    /**
     * 上锁并执行代码
     *
     * @param key      分布式锁 key
     * @param time     获取锁等待时间
     * @param timeUnit 获取锁等待时间单位
     * @param function 需要执行的代码块
     */
    public void lock(String key, long time, TimeUnit timeUnit, Param0Function function) {
        long t1 = System.currentTimeMillis();
        localLock(key, time, timeUnit, () -> {
            RLock lock = redisson.getLock(key);
            try {
                if (lock.tryLock(System.currentTimeMillis() - t1, timeUnit)) {
                    function.apply();
                } else {
                    throw new RuntimeException("get redisson lock failed");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("get redisson lock failed");
            } finally {
                if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        });
    }

    // -----------------------------------------------------------------------------------------------------------------

    /**
     * 上锁并执行代码
     *
     * @param key      分布式锁 key
     * @param function 需要执行的代码块
     */
    private void localLock(String key, Param0Function function) {
        ReentrantLock lock = reentrantLockMap.computeIfAbsent(key, k -> new ReentrantLock());
        try {
            if (lock.tryLock()) {
                function.apply();
            } else {
                throw new RuntimeException("get redisson lock failed");
            }
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        if (!lock.isLocked()) {
            reentrantLockMap.remove(key);
        }
    }

    /**
     * 上锁并执行代码
     *
     * @param key      分布式锁 key
     * @param time     获取锁等待时间
     * @param timeUnit 获取锁等待时间单位
     * @param function 需要执行的代码块
     */
    private void localLock(String key, long time, TimeUnit timeUnit, Param0Function function) {
        ReentrantLock lock = reentrantLockMap.computeIfAbsent(key, k -> new ReentrantLock());
        try {
            if (lock.tryLock(time, timeUnit)) {
                function.apply();
            } else {
                throw new RuntimeException("get redisson lock failed");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("get redisson lock failed");
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            if (!lock.isLocked()) {
                reentrantLockMap.remove(key);
            }
        }
    }


    private <T> T localLock(String key, Supplier<T> supplier) {
        ReentrantLock lock = reentrantLockMap.computeIfAbsent(key, k -> new ReentrantLock());
        try {
            if (lock.tryLock()) {
                return supplier.get();
            } else {
                throw new RuntimeException("get reentrant lock failed");
            }
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            if (!lock.isLocked()) {
                reentrantLockMap.remove(key);
            }
        }
    }

    private <T> T localLock(String key, long time, TimeUnit timeUnit, Supplier<T> supplier) {
        ReentrantLock lock = reentrantLockMap.computeIfAbsent(key, k -> new ReentrantLock());
        try {
            if (lock.tryLock(time, timeUnit)) {
                return supplier.get();
            } else {
                throw new RuntimeException("get redisson lock failed");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("get redisson lock failed");
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            if (!lock.isLocked()) {
                reentrantLockMap.remove(key);
            }
        }
    }

}