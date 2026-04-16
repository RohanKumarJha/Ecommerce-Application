package ecommerce.core.exception;

import ecommerce.core.dto.response.ExceptionResponse;
import ecommerce.core.dto.response.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------------- VALIDATION ERROR ----------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ValidationErrorResponse response =
                new ValidationErrorResponse(errors, HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ---------------- RESOURCE NOT FOUND ----------------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleResourceNotFound(ResourceNotFoundException e) {

        ExceptionResponse response =
                new ExceptionResponse(
                        e.getMessage(),
                        false,
                        HttpStatus.NOT_FOUND.value()
                );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // ---------------- BAD CREDENTIALS (WRONG PASSWORD) ----------------
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleBadCredentials(BadCredentialsException e) {

        ExceptionResponse response =
                new ExceptionResponse(
                        "Invalid username or password.",
                        false,
                        HttpStatus.UNAUTHORIZED.value()
                );

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // ---------------- CUSTOM API EXCEPTION ----------------
    @ExceptionHandler(APIException.class)
    public ResponseEntity<ExceptionResponse> handleAPIException(APIException e) {
        ExceptionResponse response =
                new ExceptionResponse(
                        e.getMessage(),
                        false,
                        HttpStatus.BAD_REQUEST.value()
                );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ---------------- UNAUTHORIZED EXCEPTION ----------------
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(401).body(ex.getMessage());
    }

    // ---------------- GLOBAL EXCEPTION ----------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGlobalException(Exception e,
                                                                   HttpServletRequest request) {
        log.error("Unexpected error occurred at {}", request.getRequestURI(), e);
        ExceptionResponse response =
                new ExceptionResponse(
                        "Something went wrong. Please try again later.",
                        false,
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}