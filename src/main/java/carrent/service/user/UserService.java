package carrent.service.user;

import carrent.dto.user.UserRegistrationRequestDto;
import carrent.dto.user.UserRegistrationResponseDto;
import carrent.exception.RegistrationException;

public interface UserService {
    UserRegistrationResponseDto registerUser(UserRegistrationRequestDto requestDto)
            throws RegistrationException;
}
