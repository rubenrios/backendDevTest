package com.rubenrbr.products.domain.exception;

public class ProductNotFoundException extends RuntimeException {

  public ProductNotFoundException(Long productId) {
    super(String.format("Product %d not found.", productId));
  }
}
