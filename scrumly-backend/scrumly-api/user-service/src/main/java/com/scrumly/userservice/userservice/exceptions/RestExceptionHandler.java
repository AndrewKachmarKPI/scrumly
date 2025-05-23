package com.scrumly.userservice.userservice.exceptions;

import com.scrumly.exceptions.model.ApiErrorDetails;
import com.scrumly.exceptions.model.ApiErrorDto;
import com.scrumly.exceptions.model.ApiValidationError;
import com.scrumly.exceptions.types.DuplicateEntityException;
import com.scrumly.exceptions.types.EntityNotFoundException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
        ApiErrorDto apiError = new ApiErrorDto(HttpStatus.NOT_FOUND.value());
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(DuplicateEntityException.class)
    protected ResponseEntity<Object> handleDuplicateEntity(DuplicateEntityException ex) {
        ApiErrorDto apiError = new ApiErrorDto(HttpStatus.CONFLICT.value());
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleGlobalException(Exception ex) {
        ApiErrorDto apiError = new ApiErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.value());
        apiError.setMessage(ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ApiErrorDetails> details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fieldError -> ApiValidationError.builder()
                        .field(fieldError.getField())
                        .object(fieldError.getObjectName())
                        .message(fieldError.getDefaultMessage())
                        .rejectedValue(fieldError.getRejectedValue())
                        .build())
                .collect(Collectors.toList());
        ApiErrorDto apiError = new ApiErrorDto(HttpStatus.BAD_REQUEST.value());
        apiError.setMessage(ex.getMessage());
        apiError.setDetails(details);
        return buildResponseEntity(apiError);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String error = "Malformed JSON request";
        return buildResponseEntity(new ApiErrorDto(HttpStatus.BAD_REQUEST.value(), error, ex));
    }

    private ResponseEntity<Object> buildResponseEntity(ApiErrorDto apiErrorDto) {
        return ResponseEntity
                .status(HttpStatus.valueOf(apiErrorDto.getStatus()))
                .body(apiErrorDto);
    }
}
