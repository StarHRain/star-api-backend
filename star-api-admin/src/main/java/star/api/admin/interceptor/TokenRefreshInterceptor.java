package star.api.admin.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import star.api.admin.exception.ThrowUtils;
import star.api.common.ErrorCode;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static star.api.admin.constant.RedisConstant.LOGIN_TOKEN_KEY;
import static star.api.admin.constant.RedisConstant.LOGIN_TOKEN_TTL;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 15 日
 */
@Slf4j
public class TokenRefreshInterceptor implements HandlerInterceptor {

    private RedisTemplate<String, Object> redisTemplate;

    public TokenRefreshInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        String tokenKey = LOGIN_TOKEN_KEY + token;
        Map<Object, Object> userMap = redisTemplate.opsForHash().entries(tokenKey);
        if (userMap != null && !userMap.isEmpty()) {
            //有缓存数据则沿用token
            redisTemplate.expire(tokenKey, LOGIN_TOKEN_TTL, TimeUnit.MINUTES);
            request.setAttribute("token", token);
        }else {
            //无缓存数据则创建新token
            token = UUID.randomUUID().toString();
            request.setAttribute("token", token);
        }
        return true;
    }
}
