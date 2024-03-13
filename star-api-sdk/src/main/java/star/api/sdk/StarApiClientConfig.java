package star.api.sdk;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import star.api.sdk.client.StarApiClient;

/**
 * @author 千树星雨
 * @date 2024年03月10日
 */

@Data
@Configuration
@ConfigurationProperties("star.client")
@ComponentScan
public class StarApiClientConfig {

    private String accessKey;

    private String secretKey;

    @Bean
    public StarApiClient getStarApiClient() {
        return new StarApiClient(accessKey, secretKey);
    }
}
