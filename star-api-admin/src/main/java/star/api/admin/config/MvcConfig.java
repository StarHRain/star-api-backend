package star.api.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * 全局跨域配置
 *
 * @author 千树星雨
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    }
}
