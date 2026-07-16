package com.dev.cs_api.core.exceptions;

import com.dev.cs_api.core.BaseExceptionHandler;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatusException(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String title = status.getReasonPhrase();
        String description = exception.getReason() != null ? exception.getReason() : "Erro de processamento na requisição";
        return createProblemDetail(
                status,
                description,
                title,
                "urn:cs-api:core:http-error-" + status.value(),
                "core"
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException exception) {
        return createProblemDetail(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                "Não Autorizado",
                "urn:cs-api:core:access-denied",
                "core"
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        return createProblemDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                exception.getMessage(),
                "Conteúdo da Requisição Inválido",
                "urn:cs-api:core:illegal-argument",
                "core"
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFoundException(
            EntityNotFoundException exception
    ) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                "Recurso não Encontrado",
                "urn:cs-api:core:entity-not-found",
                "core"
        );
    }
}
