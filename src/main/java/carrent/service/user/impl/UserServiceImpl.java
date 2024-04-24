package carrent.service.user.impl;

import carrent.dto.user.UserRegistrationRequestDto;
import carrent.dto.user.UserRegistrationResponseDto;
import carrent.exception.RegistrationException;
import carrent.mapper.UserMapper;
import carrent.model.User;
import carrent.repository.user.UserRepository;
import carrent.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserRegistrationResponseDto registerUser(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.findByEmail(requestDto.email()).isPresent()) {
            throw new RegistrationException("User with this email already exists!");
        }
        User modelUser = userMapper.toModelFromRegister(requestDto);
        return userMapper.toDtoFromModel(userRepository.save(modelUser));
    }
}
