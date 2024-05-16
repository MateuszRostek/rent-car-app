package carrent.service.car;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import carrent.dto.car.CarDto;
import carrent.dto.car.CreateCarRequestDto;
import carrent.mapper.CarMapper;
import carrent.model.Car;
import carrent.repository.car.CarRepository;
import carrent.service.car.impl.CarServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {
    private static final Long VALID_CAR_ID = 1L;
    private static final Long INVALID_CAR_ID = 100L;
    private static final BigDecimal SAMPLE_DAILY_FEE = BigDecimal.valueOf(199.99);
    private static final String SAMPLE_BRAND = "BMW";
    private static final String SAMPLE_MODEL = "X7";
    private static final Car.Type SAMPLE_TYPE = Car.Type.SUV;
    private static final String SAMPLE_INVALID_TYPE_NAME = "RANDOM TYPE";
    private static final int SAMPLE_INVENTORY = 2;
    @Mock
    private CarRepository carRepository;
    @Mock
    private CarMapper carMapper;
    @InjectMocks
    private CarServiceImpl carService;

    @Test
    @DisplayName("Save a new Car with valid request type - Returns saved Car DTO")
    void save_ValidRequestDto_ReturnsCarDto() {
        CreateCarRequestDto validRequest = createTestRequestDto(SAMPLE_TYPE.name());
        Car modelCar = createTestCarFromRequest(validRequest);
        CarDto expected = createTestCarDto(modelCar);
        when(carMapper.toModelFromCreate(validRequest)).thenReturn(modelCar);
        when(carRepository.save(modelCar)).thenReturn(modelCar);
        when(carMapper.toDtoFromModel(modelCar)).thenReturn(expected);

        CarDto actual = carService.save(validRequest);

        assertEquals(expected, actual);
        verify(carRepository, times(1)).save(any());
        verify(carMapper, times(1)).toDtoFromModel(any());
        verify(carMapper, times(1)).toModelFromCreate(any());
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("Save a new Car with invalid request type - Throws EntityNotFoundException")
    void save_InvalidRequestDto_ThrowsEntityNotFoundException() {
        CreateCarRequestDto invalidRequest = createTestRequestDto(SAMPLE_INVALID_TYPE_NAME);
        String expected = "Invalid car type, must be one of the following: "
                + Arrays.toString(Car.Type.values());

        EntityNotFoundException exception =
                assertThrows(EntityNotFoundException.class, () -> carService.save(invalidRequest));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("Find all cars with valid pageable - Returns all cars")
    void findAll_ValidPageable_ReturnsAllCarDto() {
        Car firstModelCar = createTestCar(VALID_CAR_ID);
        Car secondModelCar = createTestCar(2L);
        Car thirdModelCar = createTestCar(3L);
        CarDto firstCarDto = createTestCarDto(firstModelCar);
        CarDto secondCarDto = createTestCarDto(secondModelCar);
        CarDto thirdCarDto = createTestCarDto(thirdModelCar);
        Pageable pageable = PageRequest.of(0, 10);
        List<Car> modelCars = List.of(firstModelCar, secondModelCar, thirdModelCar);
        PageImpl<Car> modelCarsPage = new PageImpl<>(modelCars, pageable, modelCars.size());
        when(carRepository.findAll(pageable)).thenReturn(modelCarsPage);
        when(carMapper.toDtoFromModel(firstModelCar)).thenReturn(firstCarDto);
        when(carMapper.toDtoFromModel(secondModelCar)).thenReturn(secondCarDto);
        when(carMapper.toDtoFromModel(thirdModelCar)).thenReturn(thirdCarDto);
        List<CarDto> expected = List.of(firstCarDto, secondCarDto, thirdCarDto);

        List<CarDto> actual = carService.findAll(pageable);

        assertIterableEquals(expected, actual);
        verify(carRepository, times(1)).findAll(pageable);
        verify(carMapper, times(3)).toDtoFromModel(any());
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("Find a Car by valid ID - Returns Car DTO")
    void findById_ValidId_ReturnsCarDto() {
        Car modelCar = createTestCar(VALID_CAR_ID);
        CarDto expected = createTestCarDto(modelCar);
        when(carRepository.findById(VALID_CAR_ID)).thenReturn(Optional.of(modelCar));
        when(carMapper.toDtoFromModel(modelCar)).thenReturn(expected);

        CarDto actual = carService.findById(VALID_CAR_ID);

        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(carRepository, times(1)).findById(any());
        verify(carMapper, times(1)).toDtoFromModel(any());
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("Find a Car by invalid ID - Throws EntityNotFoundException")
    void findById_InvalidId_ThrowsEntityNotFoundException() {
        when(carRepository.findById(INVALID_CAR_ID)).thenReturn(Optional.empty());
        String expected = "Can't find car with id " + INVALID_CAR_ID;

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> carService.findById(INVALID_CAR_ID));
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(carRepository, times(1)).findById(any());
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    @Test
    @DisplayName("Update Car by valid ID and request"
            + " (or create new when ID doesn't exist) - Returns updated or created Car DTO")
    void updateById_ValidIdAndRequestDto_ReturnsUpdatedCarDto() {
        CreateCarRequestDto requestDto = createTestRequestDto(SAMPLE_TYPE.name());
        Car modelCar = createTestCarFromRequest(requestDto);
        CarDto expected = createTestCarDto(modelCar);
        when(carMapper.toModelFromCreate(requestDto)).thenReturn(modelCar);
        when(carRepository.save(modelCar)).thenReturn(modelCar);
        when(carMapper.toDtoFromModel(modelCar)).thenReturn(expected);

        CarDto actual = carService.updateById(VALID_CAR_ID, requestDto);

        assertEquals(expected, actual);
        verify(carRepository, times(1)).save(modelCar);
        verify(carMapper, times(1)).toModelFromCreate(requestDto);
        verify(carMapper, times(1)).toDtoFromModel(modelCar);
        verifyNoMoreInteractions(carRepository, carMapper);
    }

    private CreateCarRequestDto createTestRequestDto(String type) {
        return new CreateCarRequestDto(
                SAMPLE_MODEL,
                SAMPLE_BRAND,
                type,
                SAMPLE_INVENTORY,
                SAMPLE_DAILY_FEE);
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

    private Car createTestCarFromRequest(CreateCarRequestDto requestDto) {
        Car modelCar = new Car();
        modelCar.setId(VALID_CAR_ID);
        modelCar.setBrand(requestDto.brand());
        modelCar.setModel(requestDto.model());
        modelCar.setType(Car.Type.valueOf(requestDto.type()));
        modelCar.setDailyFee(requestDto.dailyFee());
        modelCar.setInventory(requestDto.inventory());
        return modelCar;
    }
}
