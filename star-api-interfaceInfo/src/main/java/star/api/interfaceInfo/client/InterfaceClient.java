package star.api.interfaceInfo.client;

/**
 * @author 千树星雨
 * @date 2024 年 04 月 06 日
 */

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *  OpenFeign 调用服务
 */
@FeignClient("interface-service")
public interface InterfaceClient{
    @PostMapping(value = "{path}", headers = {"X-Dye-Data=star"})
    String invokeInterface(@PathVariable String path, @RequestBody String params);
}
