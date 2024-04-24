package carrent.service.car.impl;

import carrent.dto.car.CarDto;
import carrent.dto.car.CreateCarRequestDto;
import carrent.mapper.CarMapper;
import carrent.model.Car;
import carrent.repository.car.CarRepository;
import carrent.service.car.CarService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarDto save(CreateCarRequestDto car) {
        Car modelCar = carMapper.toModelFromCreate(car);
        return carMapper.toDtoFromModel(carRepository.save(modelCar));
    }

    @Override
    public List<CarDto> findAll(Pageable pageable) {
        return carRepository.findAll(pageable).stream()
                .map(carMapper::toDtoFromModel)
                .toList();
    }

    @Override
    public CarDto findById(Long id) {
        return carMapper.toDtoFromModel(
                carRepository.findById(id).orElseThrow(
                    () -> new EntityNotFoundException("Can't find car with id " + id)));
    }

    @Override
    public CarDto updateById(Long id, CreateCarRequestDto car) {
        Car modelCar = carMapper.toModelFromCreate(car);
        modelCar.setId(id);
        return carMapper.toDtoFromModel(carRepository.save(modelCar));
    }

    @Override
    public void deleteById(Long id) {
        carRepository.deleteById(id);
    }
}
