package star.api.admin.constant;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 14 日
 */
public class RedisConstant {
    /**
     * 登录 Token
     */
    public static final String LOGIN_TOKEN_KEY = "star:api:admin:user:login:token:";
    public static final Long LOGIN_TOKEN_TTL = 5L;

    /**
     * 接口查询（分页
     */
    public static final String INTERFACE_QUERY_KEY = "star:api:admin:interface:query:current:";
    public static final Long INTERFACE_QUERY_TTL = 60L;

    /**
     * 互斥锁 Key
     */
    public static final String LOCK_INTERFACE_PAGE="star:api:admin:interface:query:lock:current:";
}
