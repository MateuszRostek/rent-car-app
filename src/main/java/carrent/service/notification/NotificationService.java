package carrent.service.notification;

import carrent.dto.rental.RentalDto;

public interface NotificationService {
    void sendRentalCreationNotification(RentalDto rental);

    void sendOverdueRentalsNotification();
}
