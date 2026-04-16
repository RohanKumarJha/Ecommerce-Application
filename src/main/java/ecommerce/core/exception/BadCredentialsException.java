package ecommerce.core.exception;

public class BadCredentialsException extends RuntimeException {

    public BadCredentialsException() {
        super("Invalid username or password");
    }

    public BadCredentialsException(String message) {
        super(message);
    }
}