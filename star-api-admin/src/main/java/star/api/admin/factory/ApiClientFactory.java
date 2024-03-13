package star.api.admin.factory;

import co.elastic.clients.ApiClient;
import lombok.Data;
import star.api.model.entity.User;
import star.api.sdk.client.StarApiClient;

/**
 * @author 千树星雨
 * @date 2024年03月09日
 */
@Data
public class ApiClientFactory {
    public static StarApiClient getApiClient(User user) {
        String accessKey = user.getAccessKey();
        String secretKey = user.getSecretKey();
        return new StarApiClient(accessKey,secretKey);
    }
}
