package com.leroy.pulsecheckapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PulseCheckApiApplication {

    static void main(String[] args) {
        SpringApplication.run(PulseCheckApiApplication.class, args);
    }

}
