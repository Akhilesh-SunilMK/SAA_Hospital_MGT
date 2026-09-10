package com.hms.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.common.security.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * FR-GW-02: validate the JWT signature and expiry before forwarding. Per-service filters
 * (com.hms.common.security.JwtAuthenticationFilter) still re-validate downstream since services
 * must remain independently securable (C5) — this is defence-in-depth at the edge.
 */
@Component
public class JwtPreValidationGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<String> publicPaths;

    public JwtPreValidationGlobalFilter(JwtUtil jwtUtil,
                                         org.springframework.core.env.Environment env) {
        this.jwtUtil = jwtUtil;
        String[] paths = env.getProperty("hms.gateway.public-paths", String[].class,
                new String[]{"/api/v1/auth/register", "/api/v1/auth/login",
                        "/api/v1/auth/refresh", "/api/v1/auth/forgot-password"});
        this.publicPaths = List.of(paths);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (publicPaths.stream().anyMatch(path::equals)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return reject(exchange, "Missing or malformed Authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtUtil.isValid(token) || jwtUtil.isExpired(token)) {
            return reject(exchange, "Invalid or expired token");
        }
        return chain.filter(exchange);
    }

    private Mono<Void> reject(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of(
                "success", false,
                "statusCode", 401,
                "message", message,
                "data", Map.of(),
                "timestamp", Instant.now().toString()
        );
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            bytes = message.getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
