package ru.svyatniy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        scanBasePackages = {
                "ru.svyatniy",
                "application",
                "domain",
                "presentation"
        }
)
@EnableJpaRepositories(
        basePackages = {
                "application",
                "domain"
        }
)
@EntityScan(
        basePackages = {
                "application",
                "domain"
        }
)
@EnableScheduling
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, orderService, ENA");

        SpringApplication.run(Main.class, args);
    }
}
