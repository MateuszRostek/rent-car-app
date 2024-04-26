package carrent.dto.user;

import java.util.Set;

public record UserInfoResponseDto(
        String email,
        String firstName,
        String lastName,
        Set<String> rolesNames) {
}
