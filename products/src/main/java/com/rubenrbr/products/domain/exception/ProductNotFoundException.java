package com.rubenrbr.products.domain.exception;

public class ProductNotFoundException extends RuntimeException {

  public ProductNotFoundException(String productId) {
    super(String.format("Product %s not found.", productId));
  }
}
