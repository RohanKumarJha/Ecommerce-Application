package com.ecommerce.core.exception;

import com.ecommerce.core.dto.response.ExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class MyGlobalExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(MyGlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> myMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Validation error in request : {}", e.getBindingResult().toString());
        Map<String,String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error)-> {
            errors.put(error.getObjectName(), error.getDefaultMessage());
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(APIException.class)
    public ResponseEntity<ExceptionResponse> myAPIException(APIException e) {
        return new ResponseEntity<>(
                new ExceptionResponse(e.getMessage(), false),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionResponse> myResourceNotFoundException(ResourceNotFoundException e) {
        String message = e.getMessage();
        ExceptionResponse apiResponse = new ExceptionResponse(message, false);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }
}