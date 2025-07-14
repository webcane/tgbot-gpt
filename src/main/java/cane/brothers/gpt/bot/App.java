package cane.brothers.gpt.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

}
