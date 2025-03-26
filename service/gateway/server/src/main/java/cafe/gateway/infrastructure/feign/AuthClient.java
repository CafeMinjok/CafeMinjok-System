package cafe.gateway.infrastructure.feign;

import cafe.auth.auth_dto.jwt.JwtClaim;
import cafe.gateway.application.AuthService;
import cafe.gateway.infrastructure.configuration.AuthFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "auth", configuration = AuthFeignConfig.class)
public interface AuthClient extends AuthService {

    @GetMapping("/internal/auth/verify")
    JwtClaim verify(@RequestHeader("Authorization") String token);
}
