package star.api.admin;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import star.api.admin.exception.BusinessException;
import star.api.common.ErrorCode;
import star.api.model.entity.User;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;


/**
 * @author 千树星雨
 * @date 2024年03月10日
 */
@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void testRedis() {

        redisTemplate.opsForValue().set("name", "张三");
        System.out.println(redisTemplate.opsForValue().get("name"));

        //获取锁 然后注册账户
        RLock lock = redissonClient.getLock("1");
        User user = new User();

        try {
            if(lock.tryLock(0,-1, TimeUnit.SECONDS)){
                System.out.println("lock.tryLock");
            }
        } catch (InterruptedException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
