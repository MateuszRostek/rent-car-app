package carrent.service.user;

import carrent.dto.user.UserInfoResponseDto;
import carrent.dto.user.UserRegistrationRequestDto;
import carrent.dto.user.UserRegistrationResponseDto;
import carrent.dto.user.UserRoleUpdateRequestDto;
import carrent.dto.user.UserUpdateRequestDto;
import carrent.exception.RegistrationException;
import carrent.model.User;
import org.springframework.security.core.Authentication;

public interface UserService {
    UserRegistrationResponseDto registerUser(UserRegistrationRequestDto requestDto)
            throws RegistrationException;

    UserInfoResponseDto getProfileInfo(Authentication authentication);

    User getUserFromAuthentication(Authentication authentication);

    UserInfoResponseDto updateProfileInfo(
            Authentication authentication, UserUpdateRequestDto requestDto);

    UserInfoResponseDto updateUserRoles(Long id, UserRoleUpdateRequestDto requestDto);
}
