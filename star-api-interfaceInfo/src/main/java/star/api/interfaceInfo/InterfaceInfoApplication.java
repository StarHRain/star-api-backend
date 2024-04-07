package star.api.interfaceInfo;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@SpringBootApplication
@MapperScan("star.api.interfaceInfo.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableDubbo
@Service
@EnableFeignClients
public class InterfaceInfoApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterfaceInfoApplication.class, args);
    }

}
