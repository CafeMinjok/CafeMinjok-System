package cafe.user.user_dto.infrastructure;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long userId;
    private String username;
    private String password;
    private String email;
    private String role;
    private BigDecimal point;

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    public enum UserRole {
        ROLE_ADMIN("관리자"),
        ROLE_MANAGER("매니저"),
        ROLE_USER("사용자");

        private final String role;
    }
}
