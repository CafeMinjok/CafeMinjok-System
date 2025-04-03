package cafe.user.server.application.service;

import cafe.user.server.domain.model.User;
import cafe.user.server.domain.repository.UserRepository;
import cafe.user.server.exception.UserErrorCode;
import cafe.user.server.exception.UserException;
import cafe.user.user_dto.infrastructure.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRole().name(),
                user.getPoint()
        );
    }
}
