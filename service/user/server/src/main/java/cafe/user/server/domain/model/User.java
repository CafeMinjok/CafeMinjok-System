package cafe.user.server.domain.model;

import cafe.user.server.domain.model.vo.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder(access = AccessLevel.PRIVATE)  // 클래스 내부에서만 빌드 사용
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "p_user")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private BigDecimal point;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
}
