package carrent.service.rental;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import carrent.dto.car.CarDto;
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
import carrent.service.rental.impl.RentalServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {
    private static final Boolean SAMPLE_ACTIVITY_STATUS = true;
    private static final Long VALID_ROLE_ID = 1L;
    private static final Long VALID_USER_ID = 1L;
    private static final Long INVALID_USER_ID = 1000L;
    private static final Long INVALID_RENTAL_ID = 1000L;
    private static final String SAMPLE_USER_DATA = "Sample User Data";
    private static final BigDecimal SAMPLE_DAILY_FEE = BigDecimal.valueOf(199.99);
    private static final Role.RoleName SAMPLE_DEFAULT_ROLE = Role.RoleName.CUSTOMER;
    private static final String SAMPLE_BRAND = "BMW";
    private static final String SAMPLE_MODEL = "X7";
    private static final Car.Type SAMPLE_TYPE = Car.Type.SUV;
    private static final int SAMPLE_INVENTORY = 1;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private RentalMapper rentalMapper;
    @Mock
    private CarRepository carRepository;
    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private RentalServiceImpl rentalService;

    @Test
    @DisplayName("Find rental by valid User and Rental ID - Returns RentalDto")
    void findRentalByUserAndId_ValidUserAndRentalId_ReturnsRentalDto() {
        Role modelRole = createTestRole();
        User modelUser = createTestUser(modelRole, VALID_USER_ID);
        Car modelCar = createTestCar(1L);
        CarDto carDto = createTestCarDto(modelCar);
        Rental modelRental = createTestRental(modelCar, modelUser);
        RentalDto expected = createTestRentalDto(modelRental, carDto);
        when(rentalRepository.findById(modelRental.getId())).thenReturn(Optional.of(modelRental));
        when(rentalMapper.toDtoFromModel(modelRental)).thenReturn(expected);

        RentalDto actual = rentalService.findRentalByUserAndId(modelUser, modelRental.getId());

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(rentalRepository, times(1)).findById(any());
        verify(rentalMapper, times(1)).toDtoFromModel(any());
        verifyNoMoreInteractions(
                rentalRepository, rentalMapper, carRepository, notificationService);
    }

    @Test
    @DisplayName("Find rental by invalid User - Throws AccessDeniedException")
    void findRentalByUserAndId_InvalidUser_ThrowsAccessDeniedException() {
        Role modelRole = createTestRole();
        User modelUserRentalOwner = createTestUser(modelRole, INVALID_USER_ID);
        User modelUserAccessing = createTestUser(modelRole, VALID_USER_ID);
        Car modelCar = createTestCar(1L);
        Rental modelRental = createTestRental(modelCar, modelUserRentalOwner);
        when(rentalRepository.findById(modelRental.getId())).thenReturn(Optional.of(modelRental));
        String expected = "This user is not allowed to access this rental";

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> rentalService.findRentalByUserAndId(modelUserAccessing, modelRental.getId()));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(rentalRepository, times(1)).findById(any());
        verifyNoMoreInteractions(
                rentalRepository, rentalMapper, carRepository, notificationService);
    }

    @Test
    @DisplayName("Find rental by invalid Rental ID - Throws EntityNotFoundException")
    void findRentalByUserAndId_InvalidRentalId_ThrowsEntityNotFoundException() {
        Role modelRole = createTestRole();
        User modelUser = createTestUser(modelRole, VALID_USER_ID);
        when(rentalRepository.findById(INVALID_RENTAL_ID)).thenReturn(Optional.empty());
        String expected = "Can't find rental by id: " + INVALID_RENTAL_ID;

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> rentalService.findRentalByUserAndId(modelUser, INVALID_RENTAL_ID));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(rentalRepository, times(1)).findById(any());
        verifyNoMoreInteractions(
                rentalRepository, rentalMapper, carRepository, notificationService);
    }

    @Test
    @DisplayName("Return rental by valid User and Rental ID - Returns RentalDto")
    void returnRentalByUserAndId_ValidUserAndRentalId_ReturnsRentalDto() {
        Role modelRole = createTestRole();
        User modelUser = createTestUser(modelRole, VALID_USER_ID);
        Car modelCar = createTestCar(1L);
        CarDto carDto = createTestCarDto(modelCar);
        Rental modelRental = createTestRental(modelCar, modelUser);
        RentalDto expected = createTestReturnedRentalDto(modelRental, carDto);
        when(rentalRepository.findById(modelRental.getId())).thenReturn(Optional.of(modelRental));
        when(carRepository.findById(modelCar.getId())).thenReturn(Optional.of(modelCar));
        when(carRepository.save(modelCar)).thenReturn(modelCar);
        when(rentalRepository.save(modelRental)).thenReturn(modelRental);
        when(rentalMapper.toDtoFromModel(modelRental)).thenReturn(expected);

        RentalDto actual = rentalService.returnRentalByUserAndId(modelUser, modelRental.getId());

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(rentalRepository, times(1)).findById(any());
        verify(carRepository, times(1)).findById(any());
        verify(carRepository, times(1)).save(any());
        verify(rentalRepository, times(1)).save(any());
        verify(rentalMapper, times(1)).toDtoFromModel(any());
        verifyNoMoreInteractions(
                rentalRepository, rentalMapper, carRepository, notificationService);
    }

    @Test
    @DisplayName("Return rental (car already returned) - Throws CarAlreadyReturnedException")
    void returnRentalByUserAndId_CarAlreadyReturned_ThrowsCarAlreadyReturnedException() {
        Role modelRole = createTestRole();
        User modelUser = createTestUser(modelRole, VALID_USER_ID);
        Car modelCar = createTestCar(1L);
        Rental modelRental = createTestRental(modelCar, modelUser);
        modelRental.setActualReturnDate(LocalDate.now());
        when(rentalRepository.findById(modelRental.getId())).thenReturn(Optional.of(modelRental));
        String expected = "This car has been already returned!";

        CarAlreadyReturnedException exception = assertThrows(
                CarAlreadyReturnedException.class,
                () -> rentalService.returnRentalByUserAndId(modelUser, modelRental.getId()));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(rentalRepository, times(1)).findById(any());
        verifyNoMoreInteractions(
                rentalRepository, rentalMapper, carRepository, notificationService);
    }

    @Test
    @DisplayName("Create new rental by valid User and request - Returns RentalDto")
    void createNewRental_ValidUserAndRequest_ReturnsRentalDto() {
        Role modelRole = createTestRole();
        User modelUser = createTestUser(modelRole, VALID_USER_ID);
        RentalRequestDto rentalRequest = new RentalRequestDto(10, 1L);
        Car modelCar = createTestCar(rentalRequest.carId());
        CarDto carDto = createTestCarDto(modelCar);
        Rental modelRental = createTestRental(modelCar, modelUser);
        modelRental.setRentalDate(LocalDate.now());
        modelRental.setReturnDate(LocalDate.now().plusDays(rentalRequest.daysOfRental()));
        RentalDto expected = createTestReturnedRentalDto(modelRental, carDto);
        expected.carInfo().setInventory(1);
        when(carRepository.findById(modelCar.getId())).thenReturn(Optional.of(modelCar));
        when(carRepository.save(modelCar)).thenReturn(modelCar);
        when(rentalRepository.save(any())).thenReturn(modelRental);
        when(rentalMapper.toDtoFromModel(modelRental)).thenReturn(expected);
        doNothing().when(notificationService).sendRentalCreationNotification(expected);

        RentalDto actual = rentalService.createNewRental(modelUser, rentalRequest);

        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
        verify(carRepository, times(1)).findById(any());
        verify(carRepository, times(1)).save(any());
        verify(rentalRepository, times(1)).save(any());
        verify(rentalMapper, times(1)).toDtoFromModel(any());
        verify(notificationService, times(1)).sendRentalCreationNotification(any());
        verifyNoMoreInteractions(
                rentalRepository, rentalMapper, carRepository, notificationService);
    }

    @Test
    @DisplayName("Create new rental (car not available) - Throws CarNotAvailableException")
    void createNewRental_CarNotAvailable_ThrowsCarNotAvailableException() {
        Role modelRole = createTestRole();
        User modelUser = createTestUser(modelRole, VALID_USER_ID);
        RentalRequestDto rentalRequest = new RentalRequestDto(
                10,
                1L);
        Car modelCar = createTestCar(rentalRequest.carId());
        modelCar.setInventory(0);
        when(carRepository.findById(modelCar.getId())).thenReturn(Optional.of(modelCar));
        String expected = "Car is currently out of stock! Please choose another";

        CarNotAvailableException exception = assertThrows(
                CarNotAvailableException.class,
                () -> rentalService.createNewRental(modelUser, rentalRequest));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(carRepository, times(1)).findById(any());
        verifyNoMoreInteractions(
                rentalRepository, rentalMapper, carRepository, notificationService);
    }

    @Test
    @DisplayName("Find all rentals (Customer accessing own rentals) "
            + "- Returns List of BasicRentalDto")
    void findAllRentalsByUserAndRentalStatus_UserAccessingOwnRentals_ReturnsListBasicRentalDto() {
        Role modelRole = createTestRole();
        User modelUser = createTestUser(modelRole, VALID_USER_ID);
        Car modelCar = createTestCar(1L);
        Rental modelRental = createTestRental(modelCar, modelUser);
        List<Rental> modelRentalsList = List.of(modelRental);
        BasicRentalDto expectedRental = createTestBasicRentalDto(modelRental);
        List<BasicRentalDto> expected = List.of(expectedRental);
        when(rentalRepository.findAllByUser(modelUser)).thenReturn(modelRentalsList);
        when(rentalMapper.toBasicDtoFromModel(modelRental)).thenReturn(expectedRental);

        List<BasicRentalDto> actual = rentalService.findAllRentalsByUserAndRentalStatus(
                modelUser, SAMPLE_ACTIVITY_STATUS, modelUser.getId());

        assertIterableEquals(expected, actual);
        verify(rentalRepository, times(1)).findAllByUser(any());
        verify(rentalMapper, times(1)).toBasicDtoFromModel(any());
        verifyNoMoreInteractions(
                rentalRepository, rentalMapper, carRepository, notificationService);
    }

    @Test
    @DisplayName("Find all rentals (Customer accessing other customer's rental) "
            + "- Throws AccessDeniedException")
    void findAllRentalsByUserAndRentalStatus_InvalidPermissions_ThrowsAccessDeniedException() {
        Role modelRole = createTestRole();
        User modelUser = createTestUser(modelRole, VALID_USER_ID);
        String expected = "This user is not allowed to access these rentals";

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> rentalService.findAllRentalsByUserAndRentalStatus(
                        modelUser, SAMPLE_ACTIVITY_STATUS, INVALID_USER_ID));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verifyNoMoreInteractions(
                rentalRepository, rentalMapper, carRepository, notificationService);
    }

    private RentalDto createTestRentalDto(Rental modelRental, CarDto carDto) {
        return new RentalDto(
                modelRental.getId(),
                modelRental.getRentalDate(),
                modelRental.getReturnDate(),
                modelRental.getActualReturnDate(),
                carDto,
                modelRental.getUser().getId());
    }

    private RentalDto createTestReturnedRentalDto(Rental modelRental, CarDto carDto) {
        return new RentalDto(
                modelRental.getId(),
                modelRental.getRentalDate(),
                modelRental.getReturnDate(),
                LocalDate.now(),
                carDto,
                modelRental.getUser().getId());
    }

    private Rental createTestRental(Car modelCar, User modelUser) {
        Rental expectedRental = new Rental();
        expectedRental.setId(1L);
        expectedRental.setCar(modelCar);
        expectedRental.setUser(modelUser);
        expectedRental.setRentalDate(LocalDate.of(2024, 1, 11));
        expectedRental.setReturnDate(LocalDate.of(2024, 1, 17));
        return expectedRental;
    }

    private Car createTestCar(Long id) {
        Car modelCar = new Car();
        modelCar.setId(id);
        modelCar.setBrand(SAMPLE_BRAND);
        modelCar.setModel(SAMPLE_MODEL);
        modelCar.setType(SAMPLE_TYPE);
        modelCar.setDailyFee(SAMPLE_DAILY_FEE);
        modelCar.setInventory(SAMPLE_INVENTORY);
        return modelCar;
    }

    private CarDto createTestCarDto(Car modelCar) {
        return new CarDto(
                modelCar.getId(),
                modelCar.getModel(),
                modelCar.getBrand(),
                modelCar.getType(),
                modelCar.getInventory(),
                modelCar.getDailyFee());
    }

    private Role createTestRole() {
        Role modelRole = new Role();
        modelRole.setId(VALID_ROLE_ID);
        modelRole.setName(SAMPLE_DEFAULT_ROLE);
        return modelRole;
    }

    private User createTestUser(Role modelRole, Long id) {
        User modelUser = new User();
        modelUser.setId(id);
        modelUser.setEmail(SAMPLE_USER_DATA);
        modelUser.setFirstName(SAMPLE_USER_DATA);
        modelUser.setLastName(SAMPLE_USER_DATA);
        modelUser.setPassword(SAMPLE_USER_DATA);
        modelUser.setRoles(Set.of(modelRole));
        return modelUser;
    }

    private BasicRentalDto createTestBasicRentalDto(Rental modelRental) {
        return new BasicRentalDto(
                modelRental.getId(),
                modelRental.getRentalDate(),
                modelRental.getReturnDate(),
                modelRental.getActualReturnDate(),
                modelRental.getCar().getId(),
                modelRental.getUser().getId());
    }
}
