package carrent.service.user.impl;

import carrent.dto.user.UserInfoResponseDto;
import carrent.dto.user.UserRegistrationRequestDto;
import carrent.dto.user.UserRegistrationResponseDto;
import carrent.dto.user.UserRoleUpdateRequestDto;
import carrent.dto.user.UserUpdateRequestDto;
import carrent.exception.RegistrationException;
import carrent.mapper.UserMapper;
import carrent.model.Role;
import carrent.model.User;
import carrent.repository.role.RoleRepository;
import carrent.repository.user.UserRepository;
import carrent.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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

    @Override
    public UserInfoResponseDto getProfileInfo(Authentication authentication) {
        User userFromDb = getUserFromAuthentication(authentication);
        return userMapper.toUserInfoDtoFromModel(userFromDb);
    }

    @Override
    public User getUserFromAuthentication(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }

    @Override
    public UserInfoResponseDto updateProfileInfo(
            Authentication authentication, UserUpdateRequestDto requestDto) {
        User userFromDb = getUserFromAuthentication(authentication);
        userFromDb.setFirstName(requestDto.firstName());
        userFromDb.setLastName(requestDto.lastName());
        return userMapper.toUserInfoDtoFromModel(userRepository.save(userFromDb));
    }

    @Override
    public UserInfoResponseDto updateUserRoles(Long id, UserRoleUpdateRequestDto requestDto) {
        User userFromDb = userRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find user with id " + id));
        HashSet<Role> newRoles = new HashSet<>();
        Set<String> roleNames = requestDto.roleNames();
        for (String roleName : roleNames) {
            newRoles.add(roleRepository.findByName(Role.RoleName.valueOf(roleName.toUpperCase()))
                    .orElseThrow(
                            () -> new EntityNotFoundException(
                                    "Can't find role with name " + roleName)));
        }
        userFromDb.setRoles(newRoles);
        return userMapper.toUserInfoDtoFromModel(userRepository.save(userFromDb));
    }
}
