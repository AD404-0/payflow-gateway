package com.payflow.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for PayFlow Gateway
 * Configures API key authentication, CORS, and security headers
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    
    public SecurityConfiguration(ApiKeyAuthenticationFilter apiKeyAuthenticationFilter) {
        this.apiKeyAuthenticationFilter = apiKeyAuthenticationFilter;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API-only application
            .csrf(AbstractHttpConfigurer::disable)
            
            // Enable CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configure session management (stateless for API)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints (paths relative to context path /api/v1)
                .requestMatchers("/payments/health").permitAll()
                .requestMatchers("/merchants").permitAll() // Allow merchant creation
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            // Add custom authentication filter
            .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Configure security headers
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                    .preload(true))
                .referrerPolicy(referrerPolicy -> referrerPolicy
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins in production
        configuration.setAllowedOriginPatterns(List.of(
            "https://*.payflow.com",
            "https://localhost:*",
            "http://localhost:*"
        ));
        
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "X-API-Key", 
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With"
        ));
        
        configuration.setExposedHeaders(Arrays.asList(
            "X-RateLimit-Remaining-Minute",
            "X-RateLimit-Remaining-Hour",
            "X-RateLimit-Limit-Minute",
            "X-RateLimit-Limit-Hour",
            "Retry-After"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight response for 1 hour
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }
}