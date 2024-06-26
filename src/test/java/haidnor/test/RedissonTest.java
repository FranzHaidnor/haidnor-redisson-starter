package haidnor.test;

import haidnor.SpringBootTestMainApplication;
import haidnor.redisson.core.RedisLock;
import haidnor.redisson.core.RedisMQService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RKeys;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = {SpringBootTestMainApplication.class})
public class RedissonTest {

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private RedisLock redisLock;

    @Autowired
    private RedisMQService redisMQService;

    @Test
    public void test_12131() throws Exception {
        /**
         * LIVE:LINEWATCH_DEPT:
         * LIVE:LINEWATCH_AREA:
         * LIVE:LINEWATCH_COMPANY:
         */
        RKeys keys = redisson.getKeys();
        keys.deleteByPattern("redisson__timeout__set:*");
        keys.deleteByPattern("redisson__idle__set:*");
        keys.deleteByPattern("redisson__execute_task_once_latch:*");
    }

    @Test
    public void test_1() throws Exception {
        RScoredSortedSet<String> zset = redisson.getScoredSortedSet("ScoredSortedSet_TEST");
        zset.add(1D, "1");
        zset.add(2D, "2");
        zset.add(3D, "3");
        zset.add(4D, "4");
    }

    @Test
    public void test_2() throws Exception {
        RScoredSortedSet<String> set = redisson.getScoredSortedSet("ScoredSortedSet_TEST");
        Double firstScore = set.firstScore();
        System.out.println(firstScore);
    }

    @Test
    public void test_3() throws Exception {
        redisMQService.send("DMQ", "message", 1, TimeUnit.DAYS);
    }

    @Test
    public void test_() throws Exception {
        HashMap<Object, Object> msg = new HashMap<>();
        msg.put("name", "张三");
        msg.put("age", 18);

        for (int i = 0; i < 100; i++) {
            redisMQService.send("test_queue", msg);
        }
    }

    @Test
    public void test_delayedQueue() throws Exception {
        HashMap<Object, Object> msg = new HashMap<>();
        msg.put("name", "张三");
        msg.put("age", 18);
        for (int i = 0; i < 100; i++) {
            redisMQService.send("test_queue", msg, 10, TimeUnit.SECONDS);
        }

        new CompletableFuture<>().get();
    }

}
