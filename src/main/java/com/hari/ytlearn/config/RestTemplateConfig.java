package com.hari.ytlearn.config;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateConfig.class);

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.info("Creating RestTemplate bean");
        // You can customize the RestTemplate here if needed
        // For example, set connection and read timeouts
        return builder
            // .setConnectTimeout(Duration.ofSeconds(5))
            // .setReadTimeout(Duration.ofSeconds(5))
            .build();
    }
}
