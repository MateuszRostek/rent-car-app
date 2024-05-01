package carrent.service.rental;

import carrent.dto.rental.BasicRentalDto;
import carrent.dto.rental.RentalDto;
import carrent.dto.rental.RentalRequestDto;
import carrent.model.User;
import java.util.List;

public interface RentalService {
    RentalDto createNewRental(User user, RentalRequestDto requestDto);

    RentalDto findRentalByUserAndId(User user, Long id);

    RentalDto returnRentalByUserAndId(User user, Long id);

    List<BasicRentalDto> findAllRentalsByUserAndRentalStatus(
            User user, Boolean isActive, Long userId);
}
