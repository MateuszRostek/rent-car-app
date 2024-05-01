package carrent.dto.rental;

import java.time.LocalDate;

public record BasicRentalDto(
        Long id,
        LocalDate rentalDate,
        LocalDate returnDate,
        LocalDate actualReturnDate,
        Long carId,
        Long userId) {
}
