package star.api.gateway.constant;

import java.util.Arrays;
import java.util.List;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 26 日
 */

/**
 * 网关常量
 */
public interface GatewayConstant {
    /**
     * IP 访问白名单
     */
    List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1","0:0:0:0:0:0:0:1", "172.28.59.162");
    /**
     * 锁
     */
    String LOCK_GATEWAY_USER = "star:api:gateway:user";
    int LOCK_REGAIN_MAX_TIME_SEC = 5;
    /**
     * 流量染色请求头（ key 和 value
     */
    String DYE_DATA_HEADER = "X-Dye-Data";
    String DYE_DATA_VALUE = "star";
}
