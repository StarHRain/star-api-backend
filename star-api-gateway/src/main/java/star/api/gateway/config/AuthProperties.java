package star.api.gateway.config;

/**
 * @author 千树星雨
 * @date 2024 年 04 月 04 日
 */

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Data
@Component
@ConfigurationProperties(prefix = "api.auth")
public class AuthProperties {
    private List<String> includePaths;
    private List<String> excludePaths;

}
