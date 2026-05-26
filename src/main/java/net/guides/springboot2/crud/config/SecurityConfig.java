package net.guides.springboot2.crud.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Security and CORS configuration (Ticket LF-201).
 *
 * Why this fixes the CORS issue:
 * 1. CORS must be processed BEFORE Spring Security rejects the preflight OPTIONS request.
 *    We configure CORS at the security filter chain level using .cors(), which registers
 *    CorsFilter before the security filters.
 * 2. Allowed origins are externalized in application.properties, not hardcoded.
 *    Dev/staging/prod each have their own via profiles.
 * 3. Preflight (OPTIONS) requests are explicitly permitted without authentication.
 *
 * Security: For this HRMS API, we disable CSRF (stateless REST API) and allow
 * all endpoints. In production, you'd add JWT/OAuth2 authentication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS with our configuration source (Ticket LF-201)
            // This ensures CorsFilter runs BEFORE Spring Security's filter chain
            .cors().configurationSource(corsConfigurationSource())
            .and()
            // Disable CSRF for stateless REST API
            .csrf().disable()
            // Stateless session - no server-side sessions
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // Allow all requests (add authentication in production)
            .authorizeRequests()
                .anyRequest().permitAll();

        return http.build();
    }

    /**
     * CORS configuration source with externalized properties (Ticket LF-201).
     * Origins, methods, and headers are configurable per environment.
     *
     * This is registered as a bean so it can be picked up by both
     * Spring Security's CorsFilter and Spring MVC's CORS handling.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allowed origins from properties (comma-separated)
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // Allowed methods
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);

        // Allowed headers
        List<String> headers = Arrays.asList(allowedHeaders.split(","));
        configuration.setAllowedHeaders(headers);

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(allowCredentials);

        // How long the browser caches preflight response (1 hour)
        configuration.setMaxAge(3600L);

        // Expose headers the client might need
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
