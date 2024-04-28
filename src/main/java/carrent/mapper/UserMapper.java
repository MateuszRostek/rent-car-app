package carrent.mapper;

import carrent.config.MapperConfig;
import carrent.dto.user.UserInfoResponseDto;
import carrent.dto.user.UserRegistrationRequestDto;
import carrent.dto.user.UserRegistrationResponseDto;
import carrent.model.Role;
import carrent.model.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModelFromRegister(UserRegistrationRequestDto registrationRequestDto);

    UserRegistrationResponseDto toDtoFromModel(User user);

    @Mapping(source = "roles", target = "rolesNames", qualifiedByName = "namesFromRoles")
    UserInfoResponseDto toUserInfoDtoFromModel(User user);

    @Named("namesFromRoles")
    default Set<String> namesFromRoles(Set<Role> roles) {
        return roles.stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());
    }
}
