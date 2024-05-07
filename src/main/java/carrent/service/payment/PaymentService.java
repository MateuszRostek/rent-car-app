package carrent.service.payment;

import carrent.dto.payment.CreatePaymentRequestDto;
import carrent.dto.payment.PaymentDto;
import carrent.model.User;
import java.util.List;

public interface PaymentService {
    PaymentDto createPaymentSession(String stripeApiKey, CreatePaymentRequestDto requestDto);

    List<PaymentDto> getAllPaymentsByUserId(User user, Long userId);
}
