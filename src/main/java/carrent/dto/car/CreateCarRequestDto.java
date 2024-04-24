package carrent.dto.car;

import carrent.model.Car;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreateCarRequestDto(
        @NotBlank
        String model,
        @NotBlank
        String brand,
        Car.Type type,
        @NotNull
        @PositiveOrZero
        int inventory,
        @NotNull
        @PositiveOrZero
        BigDecimal dailyFee) {
}
