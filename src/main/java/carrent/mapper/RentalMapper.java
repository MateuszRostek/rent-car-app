package carrent.mapper;

import carrent.config.MapperConfig;
import carrent.dto.rental.RentalDto;
import carrent.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class, uses = {CarMapper.class})
public interface RentalMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "carInfo", source = "car", qualifiedByName = "modelCarToDtoCar")
    RentalDto toDtoFromModel(Rental rental);
}
