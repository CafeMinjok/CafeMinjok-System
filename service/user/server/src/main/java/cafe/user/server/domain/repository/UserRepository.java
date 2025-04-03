package cafe.user.server.domain.repository;

import cafe.user.server.domain.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {

    Optional<User> findByUsername(String username);
}
