package carrent.dto.user;

import jakarta.validation.constraints.NotNull;
import java.util.Set;

public record UserRoleUpdateRequestDto(
        @NotNull
        Set<String> roleNames) {
}
