package com.hms.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * FR-GW-03: per-client rate limiting. In-memory token bucket keyed by client IP — adequate for a
 * single-instance gateway; a production multi-instance deployment would back this with Redis
 * (Spring Cloud Gateway's RequestRateLimiter) instead.
 */
@Component
public class RateLimitingGlobalFilter implements GlobalFilter, Ordered {

    private record Bucket(AtomicLong tokens, AtomicLong lastRefillMs) {
    }

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final long capacity;
    private final long refillPerSecond;

    public RateLimitingGlobalFilter(@Value("${hms.gateway.rate-limit.capacity:100}") long capacity,
                                     @Value("${hms.gateway.rate-limit.refill-per-second:20}") long refillPerSecond) {
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientKey = resolveClientKey(exchange.getRequest());
        Bucket bucket = buckets.computeIfAbsent(clientKey,
                k -> new Bucket(new AtomicLong(capacity), new AtomicLong(System.currentTimeMillis())));

        refill(bucket);

        if (bucket.tokens().getAndUpdate(t -> t > 0 ? t - 1 : t) <= 0) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    private void refill(Bucket bucket) {
        long now = System.currentTimeMillis();
        long last = bucket.lastRefillMs().get();
        long elapsedMs = now - last;
        if (elapsedMs <= 0) {
            return;
        }
        long tokensToAdd = (elapsedMs * refillPerSecond) / 1000;
        if (tokensToAdd > 0 && bucket.lastRefillMs().compareAndSet(last, now)) {
            bucket.tokens().updateAndGet(t -> Math.min(capacity, t + tokensToAdd));
        }
    }

    private String resolveClientKey(ServerHttpRequest request) {
        InetSocketAddress remote = request.getRemoteAddress();
        return remote != null ? remote.getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}
