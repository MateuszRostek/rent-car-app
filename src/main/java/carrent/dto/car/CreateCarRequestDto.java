package carrent.dto.car;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreateCarRequestDto(
        @NotBlank
        String model,
        @NotBlank
        String brand,
        @NotBlank
        String type,
        @PositiveOrZero
        int inventory,
        @NotNull
        @PositiveOrZero
        BigDecimal dailyFee) {

    @Override
    public String type() {
        return type.toUpperCase();
    }
}
