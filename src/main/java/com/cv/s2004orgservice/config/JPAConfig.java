package com.cv.s2004orgservice.config;

import com.cv.s10coreservice.constant.ApplicationConstant;
import com.cv.s10coreservice.util.StaticUtil;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Slf4j
@Configuration
@EntityScan(basePackages = {
        "com.cv.s10coreservice.entity",
        "com.cv.s2002orgservicepojo.entity"
})
@EnableJpaRepositories("com.cv.s2004orgservice.repository")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider", dateTimeProviderRef = "dateTimeProvider")
public class JPAConfig {

    @Bean
    @Primary
    DataSourceProperties appDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    HikariDataSource appDataSource() {
        return appDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean
    AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                return Optional.of(StaticUtil.extractHeader(ApplicationConstant.X_HEADER_USER_NAME));
            } catch (Exception e) {
                log.error("Error in getting auditor provider", e);
                return Optional.of(ApplicationConstant.APPLICATION_UNKNOWN_USER);
            }

        };
    }

    @Bean
    DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now(ZoneId.systemDefault()));
    }

}
