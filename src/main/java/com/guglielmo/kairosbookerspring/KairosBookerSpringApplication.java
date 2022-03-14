package com.guglielmo.kairosbookerspring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KairosBookerSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(KairosBookerSpringApplication.class, args);
    }


}
