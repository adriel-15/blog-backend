package com.arprojects.blog.adapters.inbound.exception_handlers;

import com.arprojects.blog.domain.exceptions.EmailAlreadyExistsException;
import com.arprojects.blog.domain.exceptions.GoogleLoginFailedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;

@ControllerAdvice
public class AuthControllerExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ProblemDetail> handleGoogleLoginFailedException(GoogleLoginFailedException ex, HttpServletRequest request){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,ex.getMessage());
        problemDetail.setTitle("Login fail");
        problemDetail.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }
}
