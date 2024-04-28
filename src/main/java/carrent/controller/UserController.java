package carrent.controller;

import carrent.dto.user.UserInfoResponseDto;
import carrent.dto.user.UserRoleUpdateRequestDto;
import carrent.dto.user.UserUpdateRequestDto;
import carrent.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PatchMapping("/me")
    public UserInfoResponseDto updateProfileInfo(
            Authentication authentication, @RequestBody @Valid UserUpdateRequestDto requestDto) {
        return userService.updateProfileInfo(authentication, requestDto);
    }

    @PutMapping("/{id}/role")
    public UserInfoResponseDto getId(
            @PathVariable Long id, @RequestBody @Valid UserRoleUpdateRequestDto requestDto) {
        return userService.updateUserRoles(id, requestDto);
    }
}
