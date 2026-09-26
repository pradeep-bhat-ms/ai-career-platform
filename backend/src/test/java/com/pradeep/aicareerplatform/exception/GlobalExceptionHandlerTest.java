package com.pradeep.aicareerplatform.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void shouldHandleIllegalArgumentException() {

        IllegalArgumentException exception =
                new IllegalArgumentException("Resume not found");

        var response = handler.handleIllegalArgument(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .containsEntry("message", "Resume not found");
    }

    @Test
    void shouldHandleTooManyRequestsException() {

        TooManyRequestsException exception =
                new TooManyRequestsException("Too many requests");

        var response = handler.handleTooManyRequests(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);

        assertThat(response.getBody())
                .containsEntry("message", "Too many requests");
    }

    @Test
    void shouldHandleValidationErrors() {

        var bindingResult =
                new org.springframework.validation.BeanPropertyBindingResult(
                        new Object(),
                        "test"
                );

        bindingResult.addError(
                new org.springframework.validation.FieldError(
                        "test",
                        "email",
                        "Email is required"
                )
        );

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(
                        null,
                        bindingResult
                );

        var response = handler.handleValidationErrors(exception);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(response.getBody())
                .containsEntry("email", "Email is required");
    }
}