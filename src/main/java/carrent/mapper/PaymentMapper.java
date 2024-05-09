package carrent.mapper;

import carrent.config.MapperConfig;
import carrent.dto.payment.PaymentDto;
import carrent.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    @Mapping(target = "rentalId", source = "rental.id")
    PaymentDto toDtoFromModel(Payment payment);
}
