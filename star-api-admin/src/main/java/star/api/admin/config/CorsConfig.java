//package star.api.admin.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import star.api.admin.interceptor.TokenRefreshInterceptor;
//
//import javax.annotation.Resource;
//
///**
// * 全局跨域配置
// *
// * @author 千树星雨
// */
//@Configuration
//public class CorsConfig implements WebMvcConfigurer {
//
//    @Resource
//    private RedisTemplate redisTemplate;
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        // 覆盖所有请求
//        registry.addMapping("/**")
//                // 允许发送 Cookie
//                .allowCredentials(true)
//                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
//                .allowedOriginPatterns("*")
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .exposedHeaders("*");
//    }
//
//}
