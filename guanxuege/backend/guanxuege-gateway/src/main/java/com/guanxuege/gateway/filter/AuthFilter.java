package com.guanxuege.gateway.filter;

import com.guanxuege.gateway.config.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 全局鉴权过滤器：
 * - 白名单（登录/注册）直接放行
 * - 其余请求必须携带合法的 Bearer Token，否则返回 401
 * - 校验通过后将 userId 透传到下游服务（X-User-Id 头）
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final List<String> WHITE_LIST = List.of("/api/auth/login", "/api/auth/register");

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // OPTIONS 预检直接放行（CORS）
        if (exchange.getRequest().getMethod() == org.springframework.http.HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }
        String path = exchange.getRequest().getURI().getPath();
        if (WHITE_LIST.stream().anyMatch(path::equalsIgnoreCase)) {
            return chain.filter(exchange);
        }

        String authorization = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        try {
            String token = authorization.substring(7);
            Long userId = JwtUtil.getUserId(token);
            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
