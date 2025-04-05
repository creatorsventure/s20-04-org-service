package com.cv.s2004orgservice.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ComponentScan({"com.cv"})
@Configuration
@EnableCaching
public class WebConfig implements WebMvcConfigurer {
}
