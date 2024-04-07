package star.api.interfaceInfo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import star.api.interfaceInfo.client.InterfaceClient;
import star.api.interfaceInfo.client.fallback.InterfaceClientFallbackFacory;

/**
 * @author 千树星雨
 * @date 2024 年 04 月 07 日
 */
@Component
public class FallbackFactoryConfig {
    @Bean
    public InterfaceClientFallbackFacory interfaceClientFallbackFacory(){
        return new InterfaceClientFallbackFacory();
    }
}
