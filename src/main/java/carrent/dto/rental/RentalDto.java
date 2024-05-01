package carrent.dto.rental;

import carrent.dto.car.CarDto;
import java.time.LocalDate;

public record RentalDto(
        Long id,
        LocalDate rentalDate,
        LocalDate returnDate,
        LocalDate actualReturnDate,
        CarDto carInfo,
        Long userId) {
}
