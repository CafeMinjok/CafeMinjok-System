package cafe.user.server.application.service;

import cafe.user.server.domain.model.User;
import cafe.user.server.domain.model.vo.UserRole;
import cafe.user.server.domain.repository.UserRepository;
import cafe.user.server.exception.UserErrorCode;
import cafe.user.server.exception.UserException;
import cafe.user.server.presentation.request.UserRequest;
import cafe.user.user_dto.infrastructure.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        UserRequest.Create request = new UserRequest.Create(
                "testuser",
                "testpassword",
                "test@example.com",
                "testnick",
                UserRole.ROLE_USER
        );

        testUser = User.create(request, "encodedPassword123");
    }

    @Test
    void getUserByUsername_Success() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // When
        UserDto result = userService.getUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUserId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getPassword(), result.getPassword());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getRole().name(), result.getRole());
        assertEquals(testUser.getPoint(), result.getPoint());

        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void getUserByUsername_UserNotFound_ThrowsException() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // When & Then
        UserException exception = assertThrows(UserException.class, () -> {
            userService.getUserByUsername(username);
        });

        assertEquals(UserErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, times(1)).findByUsername(username);
    }
}