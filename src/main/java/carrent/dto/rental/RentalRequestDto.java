package carrent.dto.rental;

import jakarta.validation.constraints.Positive;

public record RentalRequestDto(
        @Positive
        int daysOfRental,
        @Positive
        long carId) {
}
