package carrent.service.rental;

import carrent.dto.rental.RentalDto;
import carrent.dto.rental.RentalRequestDto;
import carrent.model.User;

public interface RentalService {
    RentalDto createNewRental(User user, RentalRequestDto requestDto);
}
