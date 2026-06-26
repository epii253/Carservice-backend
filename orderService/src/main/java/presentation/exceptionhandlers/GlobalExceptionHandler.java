package presentation.exceptionhandlers;

import application.services.exceptions.ConflictException;
import application.services.exceptions.NotFoundException;
import application.services.exceptions.ServiceUnavailableException;
import application.services.exceptions.UnauthorizedException;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<ErrorCustomResponse> handleGrpcError(StatusRuntimeException e) {
        return switch (e.getStatus().getCode()) {
            case NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorCustomResponse(404, "Not found", e.getMessage()));
            case UNAVAILABLE, DEADLINE_EXCEEDED -> ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ErrorCustomResponse(503, "Service temporarily unavailable", e.getMessage()));
            case UNAUTHENTICATED -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorCustomResponse(401, "Unauthorized", e.getMessage()));
            case ABORTED -> ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorCustomResponse(409, "Conflict", e.getMessage()));
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorCustomResponse(500, "Internal server error", e.getMessage()));
        };
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorCustomResponse> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorCustomResponse(404, "Not found", e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorCustomResponse> handleConflict(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorCustomResponse(409, "Conflict", e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorCustomResponse> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorCustomResponse(401, "Unauthorized", e.getMessage()));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorCustomResponse> handleServiceUnavailable(ServiceUnavailableException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorCustomResponse(503, "Service temporarily unavailable", e.getMessage()));
    }
}