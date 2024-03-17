package star.api.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import star.api.admin.interceptor.TokenRefreshInterceptor;

import javax.annotation.Resource;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 15 日
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TokenRefreshInterceptor(redisTemplate))
                .addPathPatterns("/**").order(0);
    }
}
