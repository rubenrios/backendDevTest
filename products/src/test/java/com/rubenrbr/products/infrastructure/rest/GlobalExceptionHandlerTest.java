package com.rubenrbr.products.infrastructure.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.rubenrbr.products.domain.exception.ProductNotFoundException;
import com.rubenrbr.products.infrastructure.rest.exception.GlobalExceptionHandler;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler exceptionHandler;

  @BeforeEach
  void setUp() {
    exceptionHandler = new GlobalExceptionHandler();
  }

  @Test
  void handleProductNotFoundException_shouldReturnNotFoundStatus() {
    String errorMessage = "123";
    ProductNotFoundException exception = new ProductNotFoundException(errorMessage);

    ResponseEntity<String> response = exceptionHandler.handleProductNotFoundException(exception);

    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("Product 123 not found.", response.getBody());
  }

  @Test
  void handleGeneralException_shouldReturnInternalServerErrorStatus() {
    String errorMessage = "Unexpected error occurred";
    Exception exception = new Exception(errorMessage);

    ResponseEntity<String> response = exceptionHandler.handleGeneralException(exception);

    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(errorMessage, response.getBody());
  }

  @Test
  void handleGeneralException_shouldCatchRuntimeExceptions() {
    String errorMessage = "Runtime error";
    RuntimeException exception = new RuntimeException(errorMessage);

    ResponseEntity<String> response = exceptionHandler.handleGeneralException(exception);

    assertNotNull(response);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(errorMessage, response.getBody());
  }
}
