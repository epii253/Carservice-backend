package domain.services.carBuilder;

public class IncompatibleComponentException extends RuntimeException {
    public IncompatibleComponentException(String message) {
        super(message);
    }
}
