package carrent.service.user.impl;

import carrent.dto.user.UserRegistrationRequestDto;
import carrent.dto.user.UserRegistrationResponseDto;
import carrent.exception.RegistrationException;
import carrent.mapper.UserMapper;
import carrent.model.Role;
import carrent.model.User;
import carrent.repository.role.RoleRepository;
import carrent.repository.user.UserRepository;
import carrent.service.user.UserService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Role.RoleName DEFAULT_ROLE_NAME = Role.RoleName.CUSTOMER;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserRegistrationResponseDto registerUser(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.findByEmail(requestDto.email()).isPresent()) {
            throw new RegistrationException("User with this email already exists!");
        }
        User modelUser = userMapper.toModelFromRegister(requestDto);
        modelUser.setPassword(passwordEncoder.encode(requestDto.password()));
        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE_NAME).orElseThrow(
                () -> new RegistrationException("Can't find default role: " + DEFAULT_ROLE_NAME));
        modelUser.setRoles(Set.of(defaultRole));
        return userMapper.toDtoFromModel(userRepository.save(modelUser));
    }
}
