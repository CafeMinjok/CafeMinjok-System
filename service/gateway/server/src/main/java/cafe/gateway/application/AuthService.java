package cafe.gateway.application;

import cafe.auth.auth_dto.jwt.JwtClaim;

public interface AuthService {

    JwtClaim verifyToken(String token);
}
