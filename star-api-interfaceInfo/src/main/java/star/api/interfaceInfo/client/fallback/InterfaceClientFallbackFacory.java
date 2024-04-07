package star.api.interfaceInfo.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import star.api.common.ErrorCode;
import star.api.exception.BusinessException;
import star.api.interfaceInfo.client.InterfaceClient;

/**
 * @author 千树星雨
 * @date 2024 年 04 月 07 日
 */

@Slf4j
public class InterfaceClientFallbackFacory implements FallbackFactory<InterfaceClient> {
    @Override
    public InterfaceClient create(Throwable cause) {
        return new InterfaceClient() {
            @Override
            public String invokeInterface(String path, String params) {
                log.info("接口调用失败");
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "调用接口失败");
            }
        };
    }
}
