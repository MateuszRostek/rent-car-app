package carrent.controller;

import carrent.dto.user.UserInfoResponseDto;
import carrent.dto.user.UserRoleUpdateRequestDto;
import carrent.dto.user.UserUpdateRequestDto;
import carrent.model.User;
import carrent.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Management", description = "Endpoints for managing user profiles")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Get profile information",
            description = "Retrieve the profile information of the authenticated user.")
    @GetMapping("/me")
    public UserInfoResponseDto getProfileInfo(Authentication authentication) {
        User user = userService.getUserFromAuthentication(authentication);
        return userService.getProfileInfo(user);
    }

    @Operation(
            summary = "Update profile information",
            description = "Update the profile information of the authenticated user.")
    @PatchMapping("/me")
    public UserInfoResponseDto updateProfileInfo(
            Authentication authentication, @RequestBody @Valid UserUpdateRequestDto requestDto) {
        User user = userService.getUserFromAuthentication(authentication);
        return userService.updateProfileInfo(user, requestDto);
    }

    @Operation(
            summary = "Update user roles",
            description = "Update the roles of a user identified by their ID.")
    @PreAuthorize("hasAuthority('MANAGER')")
    @PutMapping("/{id}/role")
    public UserInfoResponseDto updateUserRoles(
            @PathVariable Long id, @RequestBody @Valid UserRoleUpdateRequestDto requestDto) {
        return userService.updateUserRoles(id, requestDto);
    }
}
