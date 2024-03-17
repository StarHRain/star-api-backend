package star.api.common;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 15 日
 */

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 封装 Redis数据
 */
@Data
public class RedisData<T> {

    public RedisData() {
    }

    public RedisData(T data, LocalDateTime expireTime) {
        this.data = data;
        this.expireTime = expireTime;
    }

    /**
     * 缓存的数据
     */
    private T data;

    /**
     * 逻辑过期时间
     */
    private LocalDateTime expireTime;
}
