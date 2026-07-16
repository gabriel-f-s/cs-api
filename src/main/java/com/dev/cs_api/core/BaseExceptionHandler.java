package com.dev.cs_api.core;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

public abstract class BaseExceptionHandler {
    protected ProblemDetail createProblemDetail(
            HttpStatus httpStatus,
            String description,
            String title,
            String type,
            String module
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, description);
        problemDetail.setTitle(title);
        problemDetail.setType(URI.create(type));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("module", module);
        return problemDetail;
    }
}
