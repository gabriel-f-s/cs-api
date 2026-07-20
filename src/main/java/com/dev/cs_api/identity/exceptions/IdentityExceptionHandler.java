package com.dev.cs_api.identity.exceptions;

import com.dev.cs_api.core.BaseExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class IdentityExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException() {
        return createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "E-mail ou senha inválidos",
                "Falha na Autenticação",
                "urn:cs-api:identity:bad-credentials",
                "identity"
        );
    }

    @ExceptionHandler(LockedException.class)
    public ProblemDetail handleLockedException() {
        return createProblemDetail(
                HttpStatus.LOCKED,
                "Conta bloqueada temporariamente devido a múltiplas tentativas inválidas",
                "Conta Bloqueada Temporariamente",
                "urn:cs-api:identity:account-locked",
                "identity"
        );
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ProblemDetail handleUsernameNotFoundException(UsernameNotFoundException exception) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                "Usuário não encontrado",
                "urn:cs-api:identity:user-not-found",
                "identity"
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ProblemDetail handleInvalidTokenException(InvalidTokenException exception) {
        return createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                "Código Inválido",
                "urn:cs-api:identity:invalid-token",
                "identity"
        );
    }

    @ExceptionHandler(NoPermissionException.class)
    public ProblemDetail handleNoPermissionException(NoPermissionException exception) {
        return createProblemDetail(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                "Ação Negada",
                "urn:cs-api:identity:no-permission",
                "identity"
        );
    }
}
