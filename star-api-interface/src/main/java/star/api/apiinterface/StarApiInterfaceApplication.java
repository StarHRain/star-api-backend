package star.api.apiinterface;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;

/**
 * @author 千树星雨
 * @date 2024 年 03 月 13 日
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,})
public class StarApiInterfaceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StarApiInterfaceApplication.class, args);
    }
}