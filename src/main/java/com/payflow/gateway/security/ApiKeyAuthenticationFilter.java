package com.payflow.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.gateway.controller.ApiResponse;
import com.payflow.gateway.entity.MerchantEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Security filter for API key authentication
 * Validates API keys and sets security context for authenticated requests
 */
@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);
    
    private final ApiKeyAuthenticationService authService;
    private final ObjectMapper objectMapper;
    
    // Endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
        "/payments/health",
        "/merchants",
        "/swagger-ui",
        "/api-docs",
        "/actuator/health"
    );
    
    public ApiKeyAuthenticationFilter(ApiKeyAuthenticationService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = getClientIpAddress(request);
        
        logger.debug("Processing request: {} {} from IP: {}", method, requestPath, clientIp);
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Extract API key from header
        String apiKey = extractApiKey(request);
        
        if (apiKey == null) {
            sendAuthenticationError(response, "API key is required", "MISSING_API_KEY", HttpStatus.UNAUTHORIZED);
            return;
        }
        
        // Authenticate the API key
        ApiKeyAuthenticationService.AuthenticationResult authResult = 
            authService.authenticate(apiKey, requestPath, clientIp);
        
        if (!authResult.isSuccess()) {
            if (authResult.isRateLimited()) {
                sendRateLimitError(response, authResult.getRetryAfterSeconds());
            } else {
                sendAuthenticationError(response, authResult.getErrorMessage(), 
                                      authResult.getErrorCode(), HttpStatus.UNAUTHORIZED);
            }
            return;
        }
        
        // Set security context
        MerchantEntity merchant = authResult.getMerchant();
        setSecurityContext(merchant, apiKey);
        
        // Add rate limit headers
        addRateLimitHeaders(response, authResult.getRateLimitResult());
        
        // Add merchant info to request attributes for controllers
        request.setAttribute("merchant", merchant);
        request.setAttribute("merchantId", merchant.getId());
        
        logger.debug("Authentication successful for merchant: {} on path: {}", merchant.getId(), requestPath);
        
        // Continue with the request
        filterChain.doFilter(request, response);
    }
    
    private String extractApiKey(HttpServletRequest request) {
        // Check Authorization header first (Bearer token format)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // Check X-API-Key header
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null) {
            return apiKey;
        }
        
        // Check query parameter (less secure, but supported)
        return request.getParameter("api_key");
    }
    
    private boolean isPublicEndpoint(String requestPath) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(requestPath::startsWith);
    }
    
    private void setSecurityContext(MerchantEntity merchant, String apiKey) {
        // Create authorities based on merchant status
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_MERCHANT");
        
        // Create authentication token
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                merchant.getId(), 
                apiKey, 
                Collections.singletonList(authority)
            );
        
        // Add merchant details to authentication
        authentication.setDetails(merchant);
        
        // Set security context
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    
    private void addRateLimitHeaders(HttpServletResponse response, 
                                   ApiKeyAuthenticationService.RateLimitResult rateLimitResult) {
        if (rateLimitResult != null) {
            response.setHeader("X-RateLimit-Remaining-Minute", String.valueOf(rateLimitResult.getRemainingMinute()));
            response.setHeader("X-RateLimit-Remaining-Hour", String.valueOf(rateLimitResult.getRemainingHour()));
            response.setHeader("X-RateLimit-Limit-Minute", "1000");
            response.setHeader("X-RateLimit-Limit-Hour", "10000");
        }
    }
    
    private void sendAuthenticationError(HttpServletResponse response, String message, 
                                       String errorCode, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        ApiResponse<Object> errorResponse = ApiResponse.error(errorCode, message);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
    
    private void sendRateLimitError(HttpServletResponse response, int retryAfterSeconds) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
        
        ApiResponse<Object> errorResponse = ApiResponse.error("RATE_LIMIT_EXCEEDED", 
                                                            "Rate limit exceeded. Try again later.");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        // Check for X-Forwarded-For header (proxy/load balancer)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP if multiple are present
            return xForwardedFor.split(",")[0].trim();
        }
        
        // Check for X-Real-IP header
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Fall back to remote address
        return request.getRemoteAddr();
    }
}