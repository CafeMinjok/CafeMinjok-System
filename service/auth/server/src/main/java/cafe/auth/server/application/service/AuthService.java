package cafe.auth.server.application.service;

import cafe.auth.auth_dto.jwt.JwtClaim;
import cafe.auth.server.application.dto.AuthResponse;
import cafe.auth.server.exception.AuthErrorCode;
import cafe.auth.server.exception.AuthException;
import cafe.auth.server.infrastructure.properties.JwtProperties;
import cafe.auth.server.presentation.request.AuthRequest;
import cafe.user.user_dto.infrastructure.UserDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static cafe.auth.server.domain.JwtConstant.*;
import static cafe.auth.server.exception.AuthErrorCode.*;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, JwtProperties jwtProperties, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtProperties = jwtProperties;
        this.secretKey = createSecretKey();
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse.SignIn signIn(AuthRequest.SignIn request) {
        UserDto userData = userService.getUserByUsername(request.getUsername());

        if (userData == null
        || !passwordEncoder.matches(request.getPassword(), userData.getPassword())) {
            throw new AuthException(SIGN_IN_FAIL);
        }

        return AuthResponse.SignIn.of(
                this.createToken(
                        JwtClaim.create(userData.getUserId(), userData.getUsername(), userData.getRole())
                )
        );
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
                "HmacSHA256"
        );
    }

    private JwtClaim convert(Claims claims) {
        return JwtClaim.create(
                claims.get(USER_ID, Long.class),
                claims.get(USER_NAME, String.class),
                claims.get(USER_ROLE, String.class)
        );
    }

    private String createToken(JwtClaim jwtClaim) {
        Map<String, Object> tokenClaims = this.createClaims(jwtClaim);
        Date now = new Date(System.currentTimeMillis());
        long accessTokenExpireIn = jwtProperties.getAccessTokenExpireIn();

        return Jwts.builder()
                .claims(tokenClaims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpireIn * MILLI_SECOND))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private Map<String, Object> createClaims(JwtClaim jwtClaim) {
        return Map.of(
                USER_ID, jwtClaim.getUserId(),
                USER_NAME, jwtClaim.getUsername(),
                USER_ROLE, jwtClaim.getRole()
        );
    }
}
