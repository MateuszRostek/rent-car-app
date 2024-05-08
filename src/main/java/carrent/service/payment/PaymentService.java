package carrent.service.payment;

import carrent.dto.payment.CreatePaymentRequestDto;
import carrent.dto.payment.PaymentDto;
import carrent.dto.payment.PaymentPausedDto;
import carrent.model.Payment;
import carrent.model.User;
import java.util.List;

public interface PaymentService {
    PaymentDto createPaymentSession(String stripeApiKey, CreatePaymentRequestDto requestDto);

    List<PaymentDto> getAllPaymentsByUserId(User user, Long userId);

    PaymentPausedDto getCancelPaymentPausedMessage(Long rentalId);

    PaymentDto checkSuccessfulPayment(String stripeApiKey, Long rentalId, Payment.Type type);
}
