package carrent.mapper;

import carrent.config.MapperConfig;
import carrent.dto.car.CarDto;
import carrent.dto.car.CreateCarRequestDto;
import carrent.model.Car;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    Car toModelFromCreate(CreateCarRequestDto requestDto);

    CarDto toDtoFromModel(Car modelCar);
}
