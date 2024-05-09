package carrent.exception;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("timestamp", LocalDateTime.now());
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(this::getErrorMessage)
                .toList();
        body.put("errors", errors);
        return new ResponseEntity<>(body, headers, status);
    }

    private String getErrorMessage(ObjectError objectError) {
        if (objectError instanceof FieldError fieldError) {
            String field = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            return field + " " + message;
        }
        return objectError.getDefaultMessage();
    }

    @ExceptionHandler({EntityNotFoundException.class})
    protected ResponseEntity<Object> handleEntityNotFoundException(
            EntityNotFoundException exception) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(exception.getMessage(), LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exceptionResponse);
    }

    @ExceptionHandler({RegistrationException.class})
    protected ResponseEntity<Object> handleRegistrationException(RegistrationException exception) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(exception.getMessage(), LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exceptionResponse);
    }

    @ExceptionHandler({CarNotAvailableException.class})
    protected ResponseEntity<Object> handleCarNotAvailableException(
            CarNotAvailableException exception) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(exception.getMessage(), LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exceptionResponse);
    }

    @ExceptionHandler({AccessDeniedException.class})
    protected ResponseEntity<Object> handleAccessDeniedException(
            AccessDeniedException exception) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(exception.getMessage(), LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(exceptionResponse);
    }

    @ExceptionHandler({CarAlreadyReturnedException.class})
    protected ResponseEntity<Object> handleCarAlreadyReturnedException(
            CarAlreadyReturnedException exception) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(exception.getMessage(), LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exceptionResponse);
    }

    @ExceptionHandler({PaymentAlreadyPaidException.class})
    protected ResponseEntity<Object> handlePaymentAlreadyPaidException(
            PaymentAlreadyPaidException exception) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(exception.getMessage(), LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(exceptionResponse);
    }

    @ExceptionHandler({TooManyPaymentsException.class})
    protected ResponseEntity<Object> handleTooManyPaymentsException(
            TooManyPaymentsException exception) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(exception.getMessage(), LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exceptionResponse);
    }

    @ExceptionHandler({StripeSessionCreationException.class})
    protected ResponseEntity<Object> handleStripeSessionCreationException(
            StripeSessionCreationException exception) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(exception.getMessage(), LocalDateTime.now());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(exceptionResponse);
    }
}
