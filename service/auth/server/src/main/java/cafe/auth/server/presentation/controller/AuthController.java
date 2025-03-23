package cafe.auth.server.presentation.controller;

import cafe.domain.response.ApiResponse;
import cafe.auth.server.application.dto.AuthResponse;
import cafe.auth.server.application.service.AuthService;
import cafe.auth.server.presentation.request.AuthRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/auth")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-in")
    public ApiResponse<AuthResponse.SignIn> signIn(@RequestBody AuthRequest.SignIn request) {
        return ApiResponse.ok(authService.signIn(request));
    }

}
