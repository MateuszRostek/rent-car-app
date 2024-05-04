package carrent.dto.payment;

import carrent.model.Payment;

public record CreatePaymentRequestDto(
        Long rentalId,
        Payment.Type type) {
}
