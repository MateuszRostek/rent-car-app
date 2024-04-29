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
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
        rental.setCar(findCarById(requestDto.carId()));
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(requestDto.daysOfRental()));
        rental.setActualReturnDate(null);
        rental.setUser(user);
        RentalDto rentalDto = rentalMapper.toDtoFromModel(rentalRepository.save(rental));
        rentalDto.carInfo().setInventory(1);
        return rentalDto;
    }

    @Override
    public RentalDto findRentalByUserAndId(User user, Long id) {
        Rental rentalFromDb = rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental by id: " + id));
        if (!Objects.equals(rentalFromDb.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("This user is not allowed to access this rental");
        }
        return rentalMapper.toDtoFromModel(rentalFromDb);
    }

    private Car findCarById(Long carId) {
        Car carFromDb = carRepository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException("Can't find car with id " + carId));
        checkCarAvailability(carFromDb);
        return carFromDb;
    }

    private void checkCarAvailability(Car car) {
        if (car.getInventory() <= 0) {
            throw new CarNotAvailableException(
                    "Car is currently out of stock! Please choose another");
        }
    }
}
