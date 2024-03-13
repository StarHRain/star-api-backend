package star.api.sdk.model;

import lombok.Data;
import lombok.ToString;

/**
 * @author 千树星雨
 * @date 2024年03月10日
 */
@Data
@ToString
public class User {
    /**
     * 用户名
     */
    private String username;

    /**
     * 主机号
     */
    private String host;

}

