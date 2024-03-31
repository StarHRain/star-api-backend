package star.api.admin.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import star.api.admin.config.RedissonConfig;

import javax.annotation.Resource;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 28 日
 */
@SpringJUnitConfig
@ContextConfiguration(classes = RedissonConfig.class)
public class redisTest {
    @Resource
    private RedissonClient redissonClient;

    @Test
    public void test(){
        RLock lock = redissonClient.getLock("123");
        if (lock != null){
            System.out.println("连接成功");
        }else{
            System.out.println("nothing");
        }
    }
}
