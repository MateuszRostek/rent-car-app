package carrent.service.car;

import carrent.model.Car;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CarService {
    Car save(Car car);

    List<Car> findAll(Pageable pageable);

    Car findById(Long id);

    Car updateById(Long id, Car car);

    void deleteById(Long id);
}
