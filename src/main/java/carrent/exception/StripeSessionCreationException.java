package carrent.exception;

public class StripeSessionCreationException extends RuntimeException {
    public StripeSessionCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
