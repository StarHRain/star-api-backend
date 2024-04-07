package star.api.gateway.filter;

import cn.hutool.core.text.AntPathMatcher;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import star.api.gateway.config.AuthProperties;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static star.api.constant.RedisConstant.LOGIN_TOKEN_KEY;
import static star.api.constant.RedisConstant.LOGIN_TOKEN_TTL;
import static star.api.gateway.constant.GatewayConstant.DYE_DATA_HEADER;
import static star.api.gateway.constant.GatewayConstant.DYE_DATA_VALUE;

/**
 * 全局过滤器
 *
 * @version 1.0
 * @author: 一年星雨
 * @date: 2023/2/1
 */
@Slf4j
@Component
@Data
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Resource
    private final AuthProperties authProperties;

    @Resource
    private final RedisTemplate<String, Object> redisTemplate;


    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        // 1. 判断是否需要做登录拦截
        if(isExclude(request.getPath().toString())){
            return chain.filter(exchange);
        }

        // 2. 获取 Token，若无则拦截，响应 401
        List<String> authorization = request.getHeaders().get("Authorization");
        if (authorization == null || authorization.isEmpty()) {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        // 3. 有则刷新用户缓存
        String token = authorization.get(0);
        String tokenKey = LOGIN_TOKEN_KEY + token;
        Map<Object, Object> userMap = redisTemplate.opsForHash().entries(tokenKey);
        if (userMap != null && !userMap.isEmpty()) {
            // 刷新用户缓存
            redisTemplate.expire(tokenKey, LOGIN_TOKEN_TTL, TimeUnit.MINUTES);
        }else {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //请求转发,调用接口
        return chain.filter(exchange);
    }


    private boolean isExclude(String path) {
        for (String pathPattern : authProperties.getExcludePaths()){
            if (antPathMatcher.match(pathPattern,path)){
                return true;
            }
        }
        return false;
    }
}

