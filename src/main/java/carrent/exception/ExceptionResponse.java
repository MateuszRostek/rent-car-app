package carrent.exception;

import java.time.LocalDateTime;

public record ExceptionResponse(String message, LocalDateTime timestamp) {
}
