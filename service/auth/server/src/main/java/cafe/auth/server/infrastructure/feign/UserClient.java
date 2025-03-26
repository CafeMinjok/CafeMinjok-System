package cafe.auth.server.infrastructure.feign;

import cafe.auth.server.application.service.UserService;
import cafe.auth.server.infrastructure.configuration.UserFeignConfig;
import cafe.user.user_dto.infrastructure.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user", configuration = UserFeignConfig.class)
public interface UserClient extends UserService {

    @GetMapping("/internal/users")
    UserDto getUserByUsername(@RequestParam("username") String username);
}
