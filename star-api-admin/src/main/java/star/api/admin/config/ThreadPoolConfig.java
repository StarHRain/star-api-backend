package star.api.admin.config;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 16 日
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.concurrent.*;

import static star.api.admin.constant.ThreadPoolContanst.*;

/**
 * 线程池配置类
 */
@Configuration
public class ThreadPoolConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(){
        ThreadFactory threadFactory = new ThreadFactory(){
            private int count = 1;
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("线程" + count++);
                return thread;
            }
        };
        //创建一个有界阻塞队列
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

        //创建自定义线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                queue,
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy());

        return executor;
    }

    @PostConstruct
    public void init(){
        //初始化完成后立即启动一个核心线程
    }
}
