package cafe.auth_dto.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class JwtClaim {     // 사용자 인증 정보

    private Long userId;
    private String username;
    private String role;

    public static JwtClaim create(Long userId, String username, String role) {
        return new JwtClaim(userId, username, role);
    }
}
