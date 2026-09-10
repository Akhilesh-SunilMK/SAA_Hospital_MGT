package com.hms.emr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.hms.emr", "com.hms.common"})
@EnableFeignClients
public class EmrApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmrApplication.class, args);
    }
}
