package star.api.gateway.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Objects;

/**
 * @author 千树星雨
 * @date 2024 年 04 月 02 日
 */
@Slf4j
public class LogUtils {
    public static void logPrint(ServerHttpRequest request){
        String IP_ADDRESS = Objects.requireNonNull(request.getLocalAddress()).getHostString();
        String path = request.getPath().value();
        log.info("请求唯一标识: {}", request.getId());
        log.info("请求参数: {}", request.getQueryParams());
        log.info("请求路径: {}", path);
        log.info("请求来源主机号: {}", IP_ADDRESS);
        log.info("请求来源地址: {}", request.getRemoteAddress());
    }
}
