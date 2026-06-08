package com.catfeeder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CatFeederApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatFeederApplication.class, args);
    }
}
