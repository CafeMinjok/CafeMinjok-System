package cafe.auth.server.controller;

import cafe.auth.server.application.dto.AuthResponse;
import cafe.auth.server.application.service.AuthService;
import cafe.auth.server.exception.AuthControllerAdvice;
import cafe.auth.server.exception.AuthErrorCode;
import cafe.auth.server.exception.AuthException;
import cafe.auth.server.presentation.controller.AuthController;
import cafe.auth.server.presentation.request.AuthRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new AuthControllerAdvice())  // 추가
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void signIn_Successful() throws Exception {
        // Given
        AuthRequest.SignIn request = new AuthRequest.SignIn("testuser", "password123");
        AuthResponse.SignIn response = new AuthResponse.SignIn("valid-jwt-token");

        when(authService.signIn(any(AuthRequest.SignIn.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").value("valid-jwt-token"));

        verify(authService, times(1)).signIn(any(AuthRequest.SignIn.class));
    }

    @Test
    void signIn_InvalidCredentials_ThrowsAuthException() throws Exception {
        // Given
        AuthRequest.SignIn request = new AuthRequest.SignIn("testuser", "wrongpassword");

        when(authService.signIn(any(AuthRequest.SignIn.class)))
                .thenThrow(new AuthException(AuthErrorCode.SIGN_IN_FAIL));

        // When & Then
        ResultActions result = mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authService, times(1)).signIn(any(AuthRequest.SignIn.class));
    }
}
