package carrent.service.notification;

import carrent.dto.rental.RentalDto;
import carrent.exception.TelegramExecutionException;
import carrent.model.Rental;
import carrent.repository.rental.RentalRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramNotificationService extends TelegramLongPollingBot
        implements NotificationService {
    private static final Long CHAT_ID = -1002118105811L;
    private final String botToken;
    private final RentalRepository rentalRepository;

    public TelegramNotificationService(
            @Value("${TELEGRAM_BOT_TOKEN}") String botToken,
            RentalRepository rentalRepository) {
        this.botToken = botToken;
        this.rentalRepository = rentalRepository;
    }

    @Override
    public String getBotUsername() {
        return "rent_car_notification_bot";
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        sendText("Hey! This Bot doesn't respond to incoming messages! "
                + "- it serves to send notifications!");
    }

    @Override
    public void sendRentalCreationNotification(RentalDto rental) {
        String rentalInfo = String.format("""
                        %n
                        Rental ID: %d
                        Car ID: %d
                        Rental Date: %s
                        Return Date: %s
                        User ID: %d
                        """,
                rental.id(),
                rental.carInfo().getId(),
                rental.rentalDate(),
                rental.returnDate(),
                rental.userId());
        sendText("New rental has been created:" + rentalInfo);
    }

    @Override
    @Scheduled(cron = "0 30 6 * * ?")
    public void sendOverdueRentalsNotification() {
        LocalDate today = LocalDate.now();
        List<Rental> overdueRentals =
                rentalRepository.findAllByActualReturnDateIsNullAndReturnDateIsBefore(today);
        if (overdueRentals.isEmpty()) {
            sendText(today + " - no rentals overdue!");
            return;
        }
        sendText(buildOverdueRentalsMessage(today, overdueRentals));
    }

    private static String buildOverdueRentalsMessage(
            LocalDate date, List<Rental> overdueRentals) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(date).append(" - Rentals Overdue:")
                .append(System.lineSeparator());
        for (Rental rental : overdueRentals) {
            stringBuilder.append(System.lineSeparator())
                    .append("Rental ID: ").append(rental.getId())
                    .append(System.lineSeparator())
                    .append("User ID: ").append(rental.getUser().getId())
                    .append(System.lineSeparator())
                    .append("Car ID: ").append(rental.getCar().getId())
                    .append(System.lineSeparator())
                    .append("Return Date: ").append(rental.getReturnDate())
                    .append(System.lineSeparator());
        }
        return stringBuilder.toString();
    }

    private void sendText(String messageText) {
        SendMessage message = SendMessage.builder()
                .chatId(CHAT_ID.toString())
                .text(messageText).build();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new TelegramExecutionException(
                    "Failed to send Telegram notification: " + e.getMessage());
        }
    }
}
