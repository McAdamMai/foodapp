package com.foodapp.price_reader.exceptionhandler;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

// @ControllerAdvice is a global setting by default
@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleConstraintViolation(ConstraintViolationException exception) {
        return Map.of(
                "error", "validation_error",
                "details", exception.getConstraintViolations().stream()
                        .map(v -> Map.of(
                                "field", v.getPropertyPath().toString(),
                                "message", v.getMessage()))
                        .toList()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        return Map.of(
                "error", "validation_error",
                "details", exception.getBindingResult().getFieldErrors().stream()
                        .map(fe -> Map.of(
                                "field", fe.getField(),
                                "message", Objects.requireNonNull(fe.getDefaultMessage())))
                        .toList()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleIllegalArgument(IllegalArgumentException exception) {
        return Map.of(
                "error", "bad_request",
                "details", exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return Map.of(
                "error", "type_mismatch",
                "details", exception.getMessage()
        );
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNoSuchElement(NoSuchElementException exception) {
        return Map.of(
                "error", "not_found",
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleGeneric(Exception exception) {
        return Map.of(
                "error", "internal_server_error",
                "message", "Unexpected error"
        );
    }
}
