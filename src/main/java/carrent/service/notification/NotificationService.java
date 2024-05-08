package carrent.service.notification;

import carrent.dto.payment.PaymentDto;
import carrent.dto.rental.RentalDto;

public interface NotificationService {
    void sendRentalCreationNotification(RentalDto rental);

    void sendOverdueRentalsNotification();

    void sendSuccessfulPaymentNotification(PaymentDto payment);
}
