package net.guides.springboot2.crud.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate configuration with explicit timeouts (Ticket LF-205).
 *
 * The external government API for minimum wage rates was freezing the app
 * because there were no timeouts configured. A slow API shouldn't freeze
 * the entire application.
 *
 * These timeouts are externalized in application.properties so they can
 * be tuned per environment.
 */
@Configuration
public class RestTemplateConfig {

    @Value("${app.external-api.connect-timeout:3000}")
    private int connectTimeout;

    @Value("${app.external-api.read-timeout:5000}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(connectTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .build();
    }
}
