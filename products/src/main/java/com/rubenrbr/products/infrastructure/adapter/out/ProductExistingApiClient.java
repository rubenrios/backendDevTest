package com.rubenrbr.products.infrastructure.adapter.out;

import java.util.List;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.rubenrbr.products.domain.exception.ProductNotFoundException;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductExistingApiClient {

  private final WebClient webClient;

  public Optional<List<String>> getSimilarProductIds(String productId) {
    List<String> similarIds =
        webClient
            .get()
            .uri("/{productId}/similarids", productId)
            .retrieve()
            .onStatus(
                status -> status.equals(HttpStatus.NOT_FOUND),
                response -> Mono.error(new ProductNotFoundException(productId)))
            .onStatus(
                HttpStatusCode::is5xxServerError,
                response -> Mono.error(new RuntimeException("Error calling similarids API")))
            .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
            .block();
    return Optional.ofNullable(similarIds);
  }

  public Optional<ProductDetailDto> getProductDetail(String productId) {
    ProductDetailDto product =
        webClient
            .get()
            .uri("/{productId}", productId)
            .retrieve()
            .onStatus(
                status -> status.equals(HttpStatus.NOT_FOUND),
                response -> Mono.error(new ProductNotFoundException(productId)))
            .onStatus(
                HttpStatusCode::is5xxServerError,
                response -> Mono.error(new RuntimeException("Error calling product detail API")))
            .bodyToMono(ProductDetailDto.class)
            .block();
    return Optional.ofNullable(product);
  }
}
