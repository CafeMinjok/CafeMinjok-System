package cafe.auth.server.presentation.controller;

import cafe.auth.server.exception.AuthErrorCode;
import cafe.auth.server.exception.AuthException;
import cafe.domain.response.ApiResponse;
import cafe.auth.server.application.dto.AuthResponse;
import cafe.auth.server.application.service.AuthService;
import cafe.auth.server.presentation.request.AuthRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-in")
    public ApiResponse<AuthResponse.SignIn> signIn(@RequestBody AuthRequest.SignIn request) {
        return ApiResponse.ok(authService.signIn(request));
    }

//    @ExceptionHandler(AuthException.class)
//    public ResponseEntity<ApiResponse<Void>> handleAuthException(AuthException ex) {
//        AuthErrorCode errorCode = ex.getErrorCode();
//        ApiResponse<Void> response = ApiResponse.error(errorCode.name(), errorCode.getMessage());
//        return new ResponseEntity<>(response, errorCode.getStatus());
//    }
}
