package carrent.mapper;

import carrent.config.MapperConfig;
import carrent.dto.user.UserRegistrationRequestDto;
import carrent.dto.user.UserRegistrationResponseDto;
import carrent.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModelFromRegister(UserRegistrationRequestDto registrationRequestDto);

    UserRegistrationResponseDto toDtoFromModel(User user);
}
