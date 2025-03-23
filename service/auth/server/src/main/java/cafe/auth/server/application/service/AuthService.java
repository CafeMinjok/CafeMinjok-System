package cafe.auth.server.application.service;

import cafe.auth.auth_dto.jwt.JwtClaim;
import cafe.auth.server.application.dto.AuthResponse;
import cafe.auth.server.exception.AuthException;
import cafe.auth.server.infrastructure.properties.JwtProperties;
import cafe.auth.server.presentation.request.AuthRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;

import static cafe.auth.server.domain.JwtConstant.*;
import static cafe.auth.server.exception.AuthErrorCode.*;

@Service
public class AuthService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public AuthService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = createSecretKey();
    }

    public AuthResponse.SignIn signIn(AuthRequest.SignIn request) {
        return null;
    }

    public JwtClaim verifyToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            return this.convert(claims);
        } catch (ExpiredJwtException e) {
            throw new AuthException(TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new AuthException(INVALID_TOKEN);
        }
    }

    private SecretKey createSecretKey() {
        return new SecretKeySpec(
                jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8),
                Jwts.SIG.HS256.key().build().toString()
        );
    }

    private JwtClaim convert(Claims claims) {
        return JwtClaim.create(
                claims.get(USER_ID, Long.class),
                claims.get(USER_NAME, String.class),
                claims.get(USER_ROLE, String.class)
        );
    }
}
