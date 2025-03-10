package cafe.gateway.infrastructure.filter;

import cafe.auth_dto.jwt.JwtClaim;
import cafe.gateway.application.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static cafe.domain.jwt.JwtConstants.*;

@Slf4j
@Component
@Order(-1)  // GlobalQueueFilter 보다 먼저 적용
public class JwtAuthenticationFilter implements GlobalFilter {

    private final AuthService authService;

    // authService의 메서드가 호출될 때까지 의존성 검사를 지연
    public JwtAuthenticationFilter(@Lazy AuthService authService) { this.authService = authService; }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/api/auth/")
            || path.startsWith("/api/users/sign-up")
            || path.startsWith("/api/search")
            || path.startsWith("/api/products/search")
            || path.startsWith("/api/preorder/search")
            || path.startsWith("/api/categories/search")) {
                return chain.filter(exchange);
        }

        Optional<String> token = this.extractToken(exchange);

        if (token.isEmpty()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            JwtClaim claims = authService.verifyToken(token.get());
            this.addUserClaimsToHeaders(exchange, claims);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private void addUserClaimsToHeaders(ServerWebExchange exchange, JwtClaim claims) {
        if (claims != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String jsonClaims = objectMapper.writeValueAsString(claims);
                exchange
                        .getRequest()
                        .mutate()
                        .header(X_USER_CLAIMS.getValue(), URLEncoder.encode(jsonClaims, StandardCharsets.UTF_8))
                        .build();
            } catch (JsonProcessingException e) {
                log.error("Error processing JSON: {}", e.getMessage());
            }
        }
    }

    private Optional<String> extractToken(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION.getValue());
        if (header != null && header.startsWith(BEARER_PREFIX.getValue())) {
            return Optional.of(header.substring(BEARER_PREFIX.getValue().length()));
        }
        return Optional.empty();
    }
}
