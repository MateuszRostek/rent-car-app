package carrent.service.rental.impl;

import carrent.dto.rental.BasicRentalDto;
import carrent.dto.rental.RentalDto;
import carrent.dto.rental.RentalRequestDto;
import carrent.exception.CarAlreadyReturnedException;
import carrent.exception.CarNotAvailableException;
import carrent.mapper.RentalMapper;
import carrent.model.Car;
import carrent.model.Rental;
import carrent.model.Role;
import carrent.model.User;
import carrent.repository.car.CarRepository;
import carrent.repository.rental.RentalRepository;
import carrent.service.notification.NotificationService;
import carrent.service.rental.RentalService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private static final Role.RoleName MANAGER_ROLENAME = Role.RoleName.MANAGER;
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final CarRepository carRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RentalDto createNewRental(User user, RentalRequestDto requestDto) {
        Car carFromDb = findCarById(requestDto.carId());
        checkCarAvailability(carFromDb);
        carFromDb.setInventory(carFromDb.getInventory() - 1);
        carRepository.save(carFromDb);

        Rental rental = new Rental();
        rental.setCar(carFromDb);
        rental.setRentalDate(LocalDate.now());
        rental.setReturnDate(LocalDate.now().plusDays(requestDto.daysOfRental()));
        rental.setActualReturnDate(null);
        rental.setUser(user);
        RentalDto rentalDto = rentalMapper.toDtoFromModel(rentalRepository.save(rental));
        rentalDto.carInfo().setInventory(1);
        notificationService.sendRentalCreationNotification(rentalDto);
        return rentalDto;
    }

    @Override
    public RentalDto findRentalByUserAndId(User user, Long id) {
        Rental rentalFromDb = findRentalInDbAndValidateUser(id, user);
        RentalDto rentalDto = rentalMapper.toDtoFromModel(rentalFromDb);
        rentalDto.carInfo().setInventory(1);
        return rentalDto;
    }

    @Override
    @Transactional
    public RentalDto returnRentalByUserAndId(User user, Long id) {
        Rental rentalFromDb = findRentalInDbAndValidateUser(id, user);
        if (rentalFromDb.getActualReturnDate() != null) {
            throw new CarAlreadyReturnedException("This car has been already returned!");
        }
        Car carFromDb = findCarById(rentalFromDb.getCar().getId());
        carFromDb.setInventory(carFromDb.getInventory() + 1);
        carRepository.save(carFromDb);
        rentalFromDb.setActualReturnDate(LocalDate.now());

        RentalDto rentalDto = rentalMapper.toDtoFromModel(rentalRepository.save(rentalFromDb));
        rentalDto.carInfo().setInventory(0);
        return rentalDto;
    }

    @Override
    public List<BasicRentalDto> findAllRentalsByUserAndRentalStatus(
            User user, Boolean isActive, Long userId) {
        if (checkIfUserIsManager(user)) {
            return createRentalStreamForManager(userId)
                    .map(rentalMapper::toBasicDtoFromModel)
                    .filter(br -> filterRentalsByIsActive(br, isActive))
                    .toList();
        }
        if (userId == null || userId.equals(user.getId())) {
            return rentalRepository.findAllByUser(user).stream()
                    .map(rentalMapper::toBasicDtoFromModel)
                    .filter(br -> filterRentalsByIsActive(br, isActive))
                    .toList();
        }
        throw new AccessDeniedException("This user is not allowed to access these rentals");
    }

    private Stream<Rental> createRentalStreamForManager(Long userId) {
        return userId == null
                ? rentalRepository.findAll().stream()
                : rentalRepository.findAllByUserId(userId).stream();
    }

    private boolean filterRentalsByIsActive(BasicRentalDto basicRentalDto, Boolean isActive) {
        if (isActive == null) {
            return true;
        }
        return isActive == (basicRentalDto.actualReturnDate() == null);
    }

    private boolean checkIfUserIsManager(User user) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getName().equals(MANAGER_ROLENAME));
    }

    private Rental findRentalInDbAndValidateUser(Long id, User user) {
        Rental rentalFromDb = rentalRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find rental by id: " + id));
        if (!Objects.equals(rentalFromDb.getUser().getId(), user.getId())) {
            throw new AccessDeniedException("This user is not allowed to access this rental");
        }
        return rentalFromDb;
    }

    private Car findCarById(Long carId) {
        return carRepository.findById(carId).orElseThrow(
                () -> new EntityNotFoundException("Can't find car with id " + carId));
    }

    private void checkCarAvailability(Car car) {
        if (car.getInventory() <= 0) {
            throw new CarNotAvailableException(
                    "Car is currently out of stock! Please choose another");
        }
    }
}
