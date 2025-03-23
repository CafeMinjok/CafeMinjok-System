package cafe.gateway.infrastructure.filter;

import cafe.auth.auth_dto.jwt.JwtClaim;
import cafe.gateway.application.UserQueueService;
import cafe.gateway.infrastructure.exception.GatewayErrorCode;
import cafe.gateway.infrastructure.exception.GatewayException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static cafe.gateway.infrastructure.exception.GatewayErrorCode.*;

@Component
@Slf4j
public class GlobalQueueFilter implements GlobalFilter, Ordered {

    private final UserQueueService userQueueService;
    private final ObjectMapper objectMapper;

    public GlobalQueueFilter(UserQueueService userQueueService, ObjectMapper objectMapper) {
        this.userQueueService = userQueueService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        return extractUserId(exchange)
                .flatMap(userId -> processRequest(exchange, chain, userId));
    }

    // 필터를 거칠 필요 없는 API
    private boolean isPublicPath (String path) {
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/users/sign-up");
    }

    private Mono<String> extractUserId (ServerWebExchange exchange) {
        String encodedClaims = exchange.getRequest().getHeaders().getFirst("X_USER_CLAIMS");
        if (encodedClaims == null) {
            return Mono.error(new GatewayException(UNAUTHORIZED));
        }

        String decodedClaims = URLDecoder.decode(encodedClaims, StandardCharsets.UTF_8);
        try {
            JwtClaim claims = objectMapper.readValue(decodedClaims, JwtClaim.class);
            return Mono.just(claims.getUserId().toString());
        } catch (JsonProcessingException e) {
            return Mono.error(new GatewayException(GatewayErrorCode.BAD_REQUEST));
        }
    }

    private Mono<Void> processRequest(ServerWebExchange exchange, GatewayFilterChain chain, String userId) {
        return userQueueService.isAllowed(userId)
                .flatMap(allowed -> {
                    if (allowed) {
                        return chain.filter(exchange);
                    }
                    return userQueueService.registerUser(userId)
                            .flatMap(response -> {
                                if (response.getRank() == 0) {
                                    return chain.filter(exchange);
                                }
                                var responseHeaders = exchange.getRequest().getHeaders();
                                responseHeaders.add("X-Queue-Rank", String.valueOf(response.getRank()));
                                exchange.getResponse().setStatusCode(HttpStatus.OK);
                                return exchange.getResponse().setComplete();
                                    });
                        });
    }

    @Override
    public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
}
