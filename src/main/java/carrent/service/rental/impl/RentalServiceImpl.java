package carrent.service.rental.impl;

import carrent.dto.rental.RentalDto;
import carrent.dto.rental.RentalRequestDto;
import carrent.exception.CarNotAvailableException;
import carrent.mapper.RentalMapper;
import carrent.model.Car;
import carrent.model.Rental;
import carrent.model.User;
import carrent.repository.car.CarRepository;
import carrent.repository.rental.RentalRepository;
import carrent.service.rental.RentalService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final CarRepository carRepository;

    @Override
    public RentalDto createNewRental(User user, RentalRequestDto requestDto) {
        Rental rental = new Rental();
        rental.setCar(findAndCheckCarAvailabilityById(requestDto.carId()));
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(requestDto.daysOfRental()));
        rental.setActualReturnDate(null);
        rental.setUser(user);
        RentalDto rentalDto = rentalMapper.toDtoFromModel(rentalRepository.save(rental));
        rentalDto.carInfo().setInventory(1);
        return rentalDto;
    }

    private Car findAndCheckCarAvailabilityById(Long carId) {
        Car carFromDb = carRepository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException("Can't find car with id " + carId));
        if (carFromDb.getInventory() <= 0) {
            throw new CarNotAvailableException("Car is not available!");
        }
        return carFromDb;
    }
}
