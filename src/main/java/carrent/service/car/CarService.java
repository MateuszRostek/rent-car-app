package carrent.service.car;

import carrent.dto.car.CarDto;
import carrent.dto.car.CreateCarRequestDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarDto save(CreateCarRequestDto car);

    List<CarDto> findAll(Pageable pageable);

    CarDto findById(Long id);

    CarDto updateById(Long id, CreateCarRequestDto car);

    void deleteById(Long id);
}
