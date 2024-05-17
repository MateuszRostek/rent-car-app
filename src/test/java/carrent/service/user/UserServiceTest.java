package carrent.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import carrent.service.user.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private static final Long VALID_USER_ID = 1L;
    private static final Long INVALID_USER_ID = 1000L;
    private static final Long VALID_ROLE_ID = 1L;
    private static final String SAMPLE_EMAIL = "email@valid.com";
    private static final String SAMPLE_PASSWORD = "123456789";
    private static final String SAMPLE_FIRST_NAME = "John";
    private static final String SAMPLE_LAST_NAME = "Jackson";
    private static final Role.RoleName SAMPLE_DEFAULT_ROLE = Role.RoleName.CUSTOMER;
    private static final Role.RoleName SAMPLE_MANAGER_ROLE = Role.RoleName.MANAGER;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Register a user with valid request - Returns UserRegistrationResponseDto")
    void registerUser_ValidRequest_ReturnsRegistrationResponseDto() throws RegistrationException {
        UserRegistrationRequestDto validRequest = createTestRegistrationRequest();
        Role modelRole = createTestRole();
        User modelUser = createTestUserFromRequest(validRequest, modelRole);
        UserRegistrationResponseDto expected = createTestRegistrationResponseFromModel(modelUser);
        when(userRepository.findByEmail(validRequest.email())).thenReturn(Optional.empty());
        when(userMapper.toModelFromRegister(validRequest)).thenReturn(modelUser);
        when(passwordEncoder.encode(modelUser.getPassword())).thenReturn(modelUser.getPassword());
        when(roleRepository.findByName(SAMPLE_DEFAULT_ROLE)).thenReturn(Optional.of(modelRole));
        when(userRepository.save(modelUser)).thenReturn(modelUser);
        when(userMapper.toDtoFromModel(modelUser)).thenReturn(expected);

        UserRegistrationResponseDto actual = userService.registerUser(validRequest);

        assertEquals(expected, actual);
        verify(userRepository, times(1)).findByEmail(any());
        verify(userMapper, times(1)).toModelFromRegister(any());
        verify(passwordEncoder, times(1)).encode(any());
        verify(roleRepository, times(1)).findByName(any());
        verify(userRepository, times(1)).save(any());
        verify(userMapper, times(1)).toDtoFromModel(any());
        verifyNoMoreInteractions(userRepository, roleRepository, userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("Register a user with duplicated email - Throws RegistrationException")
    void registerUser_DuplicateEmail_ThrowsRegistrationException() {
        UserRegistrationRequestDto requestWithDuplicatedEmail =
                createTestRegistrationRequest();
        Role modelRole = createTestRole();
        User modelUser = createTestUserFromRequest(requestWithDuplicatedEmail, modelRole);
        when(userRepository.findByEmail(requestWithDuplicatedEmail.email()))
                .thenReturn(Optional.of(modelUser));
        String expected = "User with this email already exists!";

        RegistrationException exception = assertThrows(RegistrationException.class,
                () -> userService.registerUser(requestWithDuplicatedEmail));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(userRepository, times(1)).findByEmail(requestWithDuplicatedEmail.email());
        verifyNoMoreInteractions(userRepository, roleRepository, userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("Get profile info by valid user - Returns UserInfoResponseDto")
    void getProfileInfo_ValidUser_ReturnsUserInfoResponseDto() {
        Role modelRole = createTestRole();
        User modelUser = createTestUser(modelRole);
        UserInfoResponseDto expected = createTestUserInfoResponseDtoFromModel(modelUser);
        when(userMapper.toUserInfoDtoFromModel(modelUser)).thenReturn(expected);

        UserInfoResponseDto actual = userService.getProfileInfo(modelUser);

        assertEquals(expected, actual);
        verify(userMapper, times(1)).toUserInfoDtoFromModel(any());
        verifyNoMoreInteractions(userRepository, roleRepository, userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("Update profile info by valid user and request "
            + "- Returns updated UserInfoResponseDto")
    void updateProfileInfo_ValidUserAndRequest_ReturnsUserInfoResponseDto() {
        UserUpdateRequestDto validUpdateRequest = new UserUpdateRequestDto("Bob", "Smith");
        Role modelRole = createTestRole();
        User modelUser = createTestUser(modelRole);
        modelUser.setFirstName(validUpdateRequest.firstName());
        modelUser.setLastName(validUpdateRequest.lastName());
        UserInfoResponseDto expected = createTestUserInfoResponseDtoFromModel(modelUser);
        when(userRepository.save(modelUser)).thenReturn(modelUser);
        when(userMapper.toUserInfoDtoFromModel(modelUser)).thenReturn(expected);

        UserInfoResponseDto actual = userService.updateProfileInfo(modelUser, validUpdateRequest);

        assertEquals(expected, actual);
        verify(userRepository, times(1)).save(any());
        verify(userMapper, times(1)).toUserInfoDtoFromModel(any());
        verifyNoMoreInteractions(userRepository, roleRepository, userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("Update User roles by valid user ID and request "
            + "- Returns updated UserInfoResponseDto")
    void updateUserRoles_ValidIdAndRequest_ReturnsUserInfoResponseDto() {
        UserRoleUpdateRequestDto validRequest = createTestRoleUpdateRequest();
        Role modelRole = createTestRole();
        User modelUser = createTestUser(modelRole);
        List<Role> listOfRoles = createListOfRolesFromRequest(validRequest);
        modelUser.setRoles(new HashSet<>(listOfRoles));
        UserInfoResponseDto expected = createTestUserInfoResponseDtoFromModel(modelUser);
        when(userRepository.findById(modelUser.getId())).thenReturn(Optional.of(modelUser));
        when(roleRepository.getByName(SAMPLE_DEFAULT_ROLE)).thenReturn(listOfRoles.get(0));
        when(roleRepository.getByName(SAMPLE_MANAGER_ROLE)).thenReturn(listOfRoles.get(1));
        when(userRepository.save(modelUser)).thenReturn(modelUser);
        when(userMapper.toUserInfoDtoFromModel(modelUser)).thenReturn(expected);

        UserInfoResponseDto actual = userService.updateUserRoles(modelUser.getId(), validRequest);

        assertEquals(expected, actual);
        verify(userRepository, times(1)).findById(any());
        verify(roleRepository, times(2)).getByName(any());
        verify(userRepository, times(1)).save(any());
        verify(userMapper, times(1)).toUserInfoDtoFromModel(any());
        verifyNoMoreInteractions(userRepository, roleRepository, userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("Update User roles by invalid user ID - Throws EntityNotFoundException")
    void updateUserRoles_InvalidId_ThrowsEntityNotFoundException() {
        when(userRepository.findById(INVALID_USER_ID)).thenReturn(Optional.empty());
        String expected = "Can't find user with id " + INVALID_USER_ID;

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.updateUserRoles(INVALID_USER_ID, createTestRoleUpdateRequest()));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(userRepository, times(1)).findById(any());
        verifyNoMoreInteractions(userRepository, roleRepository, userMapper, passwordEncoder);
    }

    private List<Role> createListOfRolesFromRequest(UserRoleUpdateRequestDto validRequest) {
        Long freeRoleId = 10L;
        List<Role> roles = new ArrayList<>();
        for (String roleName : validRequest.roleNames()) {
            Role role = new Role();
            role.setId(freeRoleId);
            role.setName(Role.RoleName.valueOf(roleName));
            roles.add(role);
            freeRoleId++;
        }
        return roles;
    }

    private UserRoleUpdateRequestDto createTestRoleUpdateRequest() {
        return new UserRoleUpdateRequestDto(
                Set.of(
                        SAMPLE_DEFAULT_ROLE.name(),
                        SAMPLE_MANAGER_ROLE.name())
        );
    }

    private User createTestUser(Role modelRole) {
        User modelUser = new User();
        modelUser.setId(1L);
        modelUser.setEmail(SAMPLE_EMAIL);
        modelUser.setFirstName(SAMPLE_FIRST_NAME);
        modelUser.setLastName(SAMPLE_LAST_NAME);
        modelUser.setPassword(SAMPLE_PASSWORD);
        modelUser.setRoles(Set.of(modelRole));
        return modelUser;
    }

    private UserInfoResponseDto createTestUserInfoResponseDtoFromModel(User modelUser) {
        Set<String> rolesNames = modelUser.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());
        return new UserInfoResponseDto(
                modelUser.getEmail(),
                modelUser.getFirstName(),
                modelUser.getLastName(),
                rolesNames);
    }

    private UserRegistrationResponseDto createTestRegistrationResponseFromModel(
            User modelUser) {
        return new UserRegistrationResponseDto(
                modelUser.getId(),
                modelUser.getEmail(),
                modelUser.getFirstName(),
                modelUser.getLastName());
    }

    private User createTestUserFromRequest(UserRegistrationRequestDto requestDto, Role modelRole) {
        User modelUser = new User();
        modelUser.setId(VALID_USER_ID);
        modelUser.setEmail(requestDto.email());
        modelUser.setFirstName(requestDto.firstName());
        modelUser.setLastName(requestDto.lastName());
        modelUser.setPassword(requestDto.password());
        modelUser.setRoles(Set.of(modelRole));
        return modelUser;
    }

    private Role createTestRole() {
        Role modelRole = new Role();
        modelRole.setId(VALID_ROLE_ID);
        modelRole.setName(SAMPLE_DEFAULT_ROLE);
        return modelRole;
    }

    private UserRegistrationRequestDto createTestRegistrationRequest() {
        return new UserRegistrationRequestDto(
                SAMPLE_EMAIL,
                SAMPLE_PASSWORD,
                SAMPLE_PASSWORD,
                SAMPLE_FIRST_NAME,
                SAMPLE_LAST_NAME);
    }
}
