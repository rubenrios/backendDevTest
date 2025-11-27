package com.rubenrbr.products.domain.exception;

public class ExternalApiException extends RuntimeException {

  public ExternalApiException() {
    super("An error has occurred in the external data API.");
  }
}
