package cafe.auth.server.service;

import cafe.auth.server.application.dto.AuthResponse;
import cafe.auth.server.application.service.AuthService;
import cafe.auth.server.application.service.UserService;
import cafe.auth.server.exception.AuthException;
import cafe.auth.server.infrastructure.properties.JwtProperties;
import cafe.auth.server.presentation.request.AuthRequest;
import cafe.user.user_dto.infrastructure.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)   // Mockito의 엄격한 모킹 설정을 완화
public class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private PasswordEncoder passwordEncoder;

//    @InjectMocks -> @BeforeEach가 실행되기 전에 AuthService 객체가 먼저 생성되는 것을 방지하기 위함
    private AuthService authService;


    @BeforeEach
    void setUp() {
        when(jwtProperties.getSecretKey()).thenReturn("this-is-a-very-long-secret-key-for-testing");  // 유효한 값 제공
        when(jwtProperties.getAccessTokenExpireIn()).thenReturn(3600);    // 1 hour
        authService = new AuthService(userService, jwtProperties, passwordEncoder);
    }

    @Test
    void signIn_ValidCredentials_ReturnsToken() {
        // Given
        AuthRequest.SignIn request = new AuthRequest.SignIn("testUser", "password123");
        UserDto userDto = new UserDto(
                1L, "testUser", "encodedPassword", "test@email.com", "ROLE_USER", null);

        when(userService.getUserByUsername("testUser")).thenReturn(userDto);
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // When
        AuthResponse.SignIn response = authService.signIn(request);

        // Then
        assertNotNull(response.getToken());
        verify(userService, times(1)).getUserByUsername("testUser");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
    }

    @Test
    void signIn_InvalidPassword_ThrowsAuthException() {
        // Given
        AuthRequest.SignIn request = new AuthRequest.SignIn("testUser", "wrongpassword");
        UserDto userDto = new UserDto(
                1L, "testUser", "encodedPassword", "test@email.com", "ROLE_USER", null);

        when(userService.getUserByUsername("testUser")).thenReturn(userDto);
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        // When & Then
        assertThrows(AuthException.class, () -> authService.signIn(request));
        verify(userService, times(1)).getUserByUsername("testUser");
        verify(passwordEncoder, times(1)).matches("wrongpassword", "encodedPassword");
    }

    @Test
    void signIn_UserNotFound_ThrowsAuthException() {
        // Given
        AuthRequest.SignIn request = new AuthRequest.SignIn("nonexistent", "password123");
        when(userService.getUserByUsername("nonexistent")).thenReturn(null);

        // When & Then
        assertThrows(AuthException.class, () -> authService.signIn(request));
        verify(userService, times(1)).getUserByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
