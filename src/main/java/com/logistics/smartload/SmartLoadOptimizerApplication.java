package com.logistics.smartload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SmartLoadOptimizerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartLoadOptimizerApplication.class, args);
    }
}
