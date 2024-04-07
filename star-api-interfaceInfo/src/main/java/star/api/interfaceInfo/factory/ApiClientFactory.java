package star.api.interfaceInfo.factory;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import star.api.interfaceInfo.config.GatewayConfig;
import star.api.model.entity.User;
import star.api.sdk.client.StarApiClient;

import javax.annotation.Resource;

/**
 * @author 千树星雨
 * @date 2024年03月09日
 */
@Data
public class ApiClientFactory {

    public static StarApiClient getApiClient(User user,String host) {
        String accessKey = user.getAccessKey();
        String secretKey = user.getSecretKey();
        return new StarApiClient(accessKey,secretKey,host);
    }
}
