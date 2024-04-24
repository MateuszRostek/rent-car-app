package carrent.service.car.impl;

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

    @Override
    public Car save(Car car) {
        return carRepository.save(car);
    }

    @Override
    public List<Car> findAll(Pageable pageable) {
        return carRepository.findAll(pageable).toList();
    }

    @Override
    public Car findById(Long id) {
        return carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find car with id " + id));
    }

    @Override
    public Car updateById(Long id, Car car) {
        car.setId(id);
        return carRepository.save(car);
    }

    @Override
    public void deleteById(Long id) {
        carRepository.deleteById(id);
    }
}
