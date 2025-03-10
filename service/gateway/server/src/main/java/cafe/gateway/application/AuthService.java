package cafe.gateway.application;

import cafe.auth_dto.jwt.JwtClaim;

public interface AuthService {

    JwtClaim verifyToken(String token);
}
