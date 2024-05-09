package carrent.dto.payment;

import carrent.model.Payment;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentDto {
    private Long id;
    private Payment.Status status;
    private Payment.Type type;
    private Long rentalId;
    private BigDecimal amountToPay;
    private String sessionUrl;
    private String sessionId;
}
