package cafe.auth.server.application.service;

import cafe.user.user_dto.infrastructure.UserDto;

public interface UserService {

    UserDto getUserByUsername(String username);
}
