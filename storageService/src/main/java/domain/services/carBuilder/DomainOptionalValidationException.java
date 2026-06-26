package domain.services.carBuilder;

public class DomainOptionalValidationException extends RuntimeException {
    public DomainOptionalValidationException(String message) {
        super(message);
    }
}
