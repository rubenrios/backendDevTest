package com.rubenrbr.products.domain.exception;

public class InvalidProductRequestException extends RuntimeException {

  public InvalidProductRequestException(String productId) {
    super(String.format("Product %s is not valid.", productId));
  }
}
