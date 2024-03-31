package star.api.admin.constant;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 14 日
 */
public interface RedisConstant {
    /**
     * 登录 Token
     */
    String LOGIN_TOKEN_KEY = "admin:user:login:token:";
    Long LOGIN_TOKEN_TTL = 5L;

    /**
     * 接口查询（分页
     */
    String INTERFACE_QUERY_KEY = "admin:interface:query:current:";
    Long INTERFACE_QUERY_TTL = 60L;
    String LOCK_INTERFACE_PAGE = "admin:interface:query:lock:current:";

    /**
     * 缓存预热
     */
    String LOCK_CACHE_PREHEAT = "admin:cache:preheat:current:";
}
