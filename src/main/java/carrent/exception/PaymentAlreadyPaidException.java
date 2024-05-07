package carrent.exception;

public class PaymentAlreadyPaidException extends RuntimeException {
    public PaymentAlreadyPaidException(String message) {
        super(message);
    }
}
