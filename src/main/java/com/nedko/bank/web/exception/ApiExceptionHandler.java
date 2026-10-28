package com.nedko.bank.web.exception;

import com.nedko.bank.account.exception.AccountNotFoundException;
import com.nedko.bank.account.exception.CurrencyMismatchException;
import com.nedko.bank.account.exception.InsufficientFundsException;
import com.nedko.bank.account.exception.SameAccountException;
import com.nedko.bank.currency.exception.CurrencyNotFoundException;
import com.nedko.bank.web.dto.ApiErrorResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    
    @ExceptionHandler({AccountNotFoundException.class, CurrencyNotFoundException.class})
    ResponseEntity<ApiErrorResponse> notFound(final RuntimeException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({CurrencyMismatchException.class, InsufficientFundsException.class, SameAccountException.class})
    ResponseEntity<ApiErrorResponse> conflict(final RuntimeException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(final MethodArgumentNotValidException exception) {
        final Map<String, List<String>> errors = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(error -> error.getField(),
                        Collectors.mapping(error -> error.getDefaultMessage(), Collectors.toList())));
        return error(HttpStatus.BAD_REQUEST, "Request validation failed.", errors);
    }

    private ResponseEntity<ApiErrorResponse> error(final HttpStatus status, final String message) {
        return error(status, message, null);
    }

    private ResponseEntity<ApiErrorResponse> error(final HttpStatus status, final String message,
            final Map<String, List<String>> errors) {
        final ApiErrorResponse response = new ApiErrorResponse(status.value(), status.getReasonPhrase(),
                message, Instant.now(), errors);
        return ResponseEntity.status(status).body(response);
    }
}
