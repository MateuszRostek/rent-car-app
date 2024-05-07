package carrent.exception;

public class TooManyPaymentsException extends RuntimeException {
    public TooManyPaymentsException(String message) {
        super(message);
    }
}
