package org.example.web.controller;

import org.example.core.KernelResp;
import org.example.core.auth.exception.InsufficientPriorityException;
import org.example.core.auth.exception.NotLoggedInException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(NotLoggedInException.class)
    public ResponseEntity<KernelResp<Void>> notLoggedIn(NotLoggedInException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(KernelResp.error(HttpStatus.UNAUTHORIZED.value(), e.getMessage()));
    }

    @ExceptionHandler(InsufficientPriorityException.class)
    public ResponseEntity<KernelResp<Void>> insufficientPriority(InsufficientPriorityException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(KernelResp.error(HttpStatus.FORBIDDEN.value(), e.getMessage()));
    }
}
