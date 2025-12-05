package com.rubenrbr.products.infrastructure.adapter.out;

import java.util.Collections;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.rubenrbr.products.domain.exception.ExternalApiException;
import com.rubenrbr.products.domain.exception.ProductNotFoundException;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductExistingApiClient {

  private final WebClient webClient;

  @Cacheable(value = "similar-ids", key = "#productId")
  @CircuitBreaker(name = "product-similar-ids", fallbackMethod = "getSimilarProductIdsFallback")
  @Retry(name = "product-similar-ids")
  @RateLimiter(name = "product-similar-ids")
  public Mono<List<String>> getSimilarProductIds(String productId) {
    return webClient
        .get()
        .uri("/{productId}/similarids", productId)
        .retrieve()
        .onStatus(
            status -> status.value() == 404,
            response -> Mono.error(new ProductNotFoundException(productId)))
        .onStatus(
            HttpStatusCode::is5xxServerError, response -> Mono.error(new ExternalApiException()))
        .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
        .defaultIfEmpty(Collections.emptyList());
  }

  @SuppressWarnings("unused")
  private Mono<List<String>> getSimilarProductIdsFallback(String productId, Throwable ex) {
    if (ex instanceof ProductNotFoundException) {
      return Mono.error(ex);
    }
    return Mono.error(new ExternalApiException());
  }

  @Cacheable(value = "product-detail", key = "#productId")
  @CircuitBreaker(name = "product-detail", fallbackMethod = "getProductDetailFallback")
  @Retry(name = "product-detail")
  @RateLimiter(name = "product-detail")
  public Mono<ProductDetailDto> getProductDetail(String productId) {
    return webClient
        .get()
        .uri("/{productId}", productId)
        .retrieve()
        .onStatus(
            status -> status.value() == 404,
            response -> Mono.error(new ProductNotFoundException(productId)))
        .onStatus(
            HttpStatusCode::is5xxServerError, response -> Mono.error(new ExternalApiException()))
        .bodyToMono(ProductDetailDto.class);
  }

  @SuppressWarnings("unused")
  private Mono<ProductDetailDto> getProductDetailFallback(String productId, Throwable ex) {
    if (ex instanceof ProductNotFoundException) {
      return Mono.error(ex);
    }
    return Mono.error(new ExternalApiException());
  }
}
