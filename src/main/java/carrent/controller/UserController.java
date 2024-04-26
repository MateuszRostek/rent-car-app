package carrent.controller;

import carrent.dto.user.UserInfoResponseDto;
import carrent.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public UserInfoResponseDto getProfileInfo(Authentication authentication) {
        return userService.getProfileInfo(authentication);
    }
}
