package com.hms.gateway.config;

import com.hms.common.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manually wires JwtUtil instead of component-scanning com.hms.common.security, because that
 * package also holds JwtAuthenticationFilter (a servlet OncePerRequestFilter) which cannot be
 * classloaded here — this gateway runs on WebFlux/Netty with no servlet API on the classpath.
 */
@Configuration
public class JwtConfig {

    @Bean
    public JwtUtil jwtUtil(@Value("${jwt.secret}") String secret) {
        return new JwtUtil(secret);
    }
}
