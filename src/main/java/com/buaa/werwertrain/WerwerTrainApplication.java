package com.buaa.werwertrain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class WerwerTrainApplication {

    public static void main(String[] args) {
        SpringApplication.run(WerwerTrainApplication.class, args);
    }

}
