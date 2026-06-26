package presentation.exceptionhandlers;

public record ErrorCustomResponse(int status, String error, String message) {}