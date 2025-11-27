package com.rubenrbr.products.infrastructure.adapter.out;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.rubenrbr.products.domain.exception.ExternalApiException;
import com.rubenrbr.products.domain.exception.InvalidProductRequestException;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductExistingApiClient {

  private final WebClient webClient;

  @Cacheable(value = "similar-ids", key = "#productId")
  public Optional<List<String>> getSimilarProductIds(String productId) {
    try {
      List<String> similarIds =
          webClient
              .get()
              .uri("/{productId}/similarids", productId)
              .retrieve()
              .onStatus(
                  status -> status.value() == 400,
                  response -> Mono.error(new InvalidProductRequestException(productId)))
              .onStatus(
                  HttpStatusCode::is5xxServerError,
                  response -> Mono.error(new ExternalApiException()))
              .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
              .block();
      return Optional.ofNullable(similarIds);
    } catch (RuntimeException e) {
      throw new ExternalApiException();
    }
  }

  @Cacheable(value = "product-detail", key = "#productId")
  public Optional<ProductDetailDto> getProductDetail(String productId) {
    try {
      ProductDetailDto dto =
          webClient
              .get()
              .uri("/{productId}", productId)
              .retrieve()
              .onStatus(
                  status -> status.value() == 400,
                  response -> Mono.error(new InvalidProductRequestException(productId)))
              .onStatus(
                  HttpStatusCode::is5xxServerError,
                  response -> Mono.error(new ExternalApiException()))
              .bodyToMono(ProductDetailDto.class)
              .block();

      return Optional.ofNullable(dto);

    } catch (WebClientResponseException.NotFound e) {
      return Optional.empty();
    }
  }
}
