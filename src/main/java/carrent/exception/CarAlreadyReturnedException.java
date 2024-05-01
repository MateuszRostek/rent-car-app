package carrent.exception;

public class CarAlreadyReturnedException extends RuntimeException {
    public CarAlreadyReturnedException(String message) {
        super(message);
    }
}
