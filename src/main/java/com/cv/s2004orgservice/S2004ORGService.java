package com.cv.s2004orgservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class S2004ORGService {

    public static void main(String[] args) {
        SpringApplication.run(S2004ORGService.class, args);
    }

}
