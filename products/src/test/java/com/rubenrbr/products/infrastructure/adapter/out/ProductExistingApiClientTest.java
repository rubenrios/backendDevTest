package com.rubenrbr.products.infrastructure.adapter.out;

import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetailDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.rubenrbr.products.domain.exception.ExternalApiException;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductExistingApiClient Unit Tests")
class ProductExistingApiClientTest {

  @Mock private WebClient webClient;

  @Mock private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

  @Mock private WebClient.RequestHeadersSpec requestHeadersSpec;

  @Mock private WebClient.ResponseSpec responseSpec;

  private ProductExistingApiClient apiClient;

  @BeforeEach
  void setUp() {
    apiClient = new ProductExistingApiClient(webClient);
  }

  @Nested
  @DisplayName("getSimilarProductIds Tests")
  class GetSimilarProductIdsTests {

    @Test
    @DisplayName("Should return list of similar product IDs on successful request")
    void shouldReturnListOfSimilarProductIds() {
      String productId = "100";
      List<String> expectedIds = List.of("1", "2", "3");

      when(webClient.get()).thenReturn(requestHeadersUriSpec);
      when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
      when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
      when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
      when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
          .thenReturn(Mono.just(expectedIds));

      Mono<List<String>> result = apiClient.getSimilarProductIds(productId);

      StepVerifier.create(result)
          .assertNext(
              ids -> {
                assertThat(ids).hasSize(3);
                assertThat(ids).containsExactly("1", "2", "3");
              })
          .verifyComplete();

      verify(webClient).get();
      verify(requestHeadersUriSpec).uri("/{productId}/similarids", productId);
    }

    @Test
    @DisplayName("Should return empty list when API returns empty response")
    void shouldReturnEmptyListWhenApiReturnsEmpty() {
      String productId = "100";

      when(webClient.get()).thenReturn(requestHeadersUriSpec);
      when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
      when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
      when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
      when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
          .thenReturn(Mono.empty());

      Mono<List<String>> result = apiClient.getSimilarProductIds(productId);

      StepVerifier.create(result).assertNext(ids -> assertThat(ids).isEmpty()).verifyComplete();
    }

    @Test
    @DisplayName("Should throw ExternalApiException on 400 status (caught by onErrorResume)")
    void shouldThrowExternalApiExceptionOn400() {
      String productId = "invalid";
      WebClientResponseException ex =
          WebClientResponseException.create(
              HttpStatus.BAD_REQUEST.value(), "Bad Request", null, null, null);

      when(webClient.get()).thenReturn(requestHeadersUriSpec);
      when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
      when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
      when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
      when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
          .thenReturn(Mono.error(ex));

      Mono<List<String>> result = apiClient.getSimilarProductIds(productId);

      StepVerifier.create(result).expectError(ExternalApiException.class).verify();
    }

    @Test
    @DisplayName("Should throw ExternalApiException on 5xx status")
    void shouldThrowExternalApiExceptionOn5xx() {
      String productId = "100";
      WebClientResponseException ex =
          WebClientResponseException.create(
              HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null);

      when(webClient.get()).thenReturn(requestHeadersUriSpec);
      when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
      when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
      when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
      when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
          .thenReturn(Mono.error(ex));

      Mono<List<String>> result = apiClient.getSimilarProductIds(productId);

      StepVerifier.create(result).expectError(ExternalApiException.class).verify();
    }

    @Test
    @DisplayName("Should throw ExternalApiException on WebClientException")
    void shouldThrowExternalApiExceptionOnWebClientException() {
      String productId = "100";
      WebClientException ex = new WebClientException("Connection timeout") {};

      when(webClient.get()).thenReturn(requestHeadersUriSpec);
      when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
      when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
      when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
      when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
          .thenReturn(Mono.error(ex));

      Mono<List<String>> result = apiClient.getSimilarProductIds(productId);

      StepVerifier.create(result).expectError(ExternalApiException.class).verify();
    }

    @Test
    @DisplayName("Should handle single product ID in list")
    void shouldHandleSingleProductIdInList() {
      String productId = "100";
      List<String> expectedIds = List.of("1");

      when(webClient.get()).thenReturn(requestHeadersUriSpec);
      when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
      when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
      when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
      when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
          .thenReturn(Mono.just(expectedIds));

      Mono<List<String>> result = apiClient.getSimilarProductIds(productId);

      StepVerifier.create(result)
          .assertNext(
              ids -> {
                assertThat(ids).hasSize(1);
                assertThat(ids).containsExactly("1");
              })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("getProductDetail Tests")
  class GetProductDetailTests {

    @Test
    @DisplayName("Should return product detail on successful request")
    void shouldReturnProductDetail() {
      String productId = "1";
      ProductDetailDto expectedProduct =
          createProductDetailDto("1", "Test Product", BigDecimal.valueOf(99.99), true);

      when(webClient.get()).thenReturn(requestHeadersUriSpec);
      when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
      when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
      when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
      when(responseSpec.bodyToMono(ProductDetailDto.class)).thenReturn(Mono.just(expectedProduct));

      Mono<ProductDetailDto> result = apiClient.getProductDetail(productId);

      StepVerifier.create(result)
          .assertNext(
              product -> {
                assertThat(product.getId()).isEqualTo("1");
                assertThat(product.getName()).isEqualTo("Test Product");
                assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
                assertThat(product.getAvailability()).isTrue();
              })
          .verifyComplete();

      verify(webClient).get();
      verify(requestHeadersUriSpec).uri("/{productId}", productId);
    }

    @Test
    @DisplayName("Should return empty Mono when product not found (404)")
    void shouldReturnEmptyMonoWhenProductNotFound() {
      String productId = "999";
      WebClientResponseException notFoundException =
          WebClientResponseException.create(
              HttpStatus.NOT_FOUND.value(), "Not Found", null, null, null);

      when(webClient.get()).thenReturn(requestHeadersUriSpec);
      when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
      when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
      when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
      when(responseSpec.bodyToMono(ProductDetailDto.class))
          .thenReturn(Mono.error(notFoundException));

      Mono<ProductDetailDto> result = apiClient.getProductDetail(productId);

      StepVerifier.create(result).verifyComplete();
    }

    @Test
    @DisplayName("Should throw ExternalApiException on 400 status")
    void shouldThrowExternalApiExceptionOn400() {
      String productId = "invalid";
      WebClientResponseException ex =
          WebClientResponseException.create(
              HttpStatus.BAD_REQUEST.value(), "Bad Request", null, null, null);

      when(webClient.get()).thenReturn(requestHeadersUriSpec);
      when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
      when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
      when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
      when(responseSpec.bodyToMono(ProductDetailDto.class)).thenReturn(Mono.error(ex));

      Mono<ProductDetailDto> result = apiClient.getProductDetail(productId);

      StepVerifier.create(result).expectError(WebClientResponseException.class).verify();
    }

    @Test
    @DisplayName("Should throw ExternalApiException on 5xx status")
    void shouldThrowExternalApiExceptionOn5xx() {
      String productId = "1";
      WebClientResponseException ex =
          WebClientResponseException.create(
              HttpStatus.SERVICE_UNAVAILABLE.value(), "Service Unavailable", null, null, null);

      when(webClient.get()).thenReturn(requestHeadersUriSpec);
      when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
      when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
      when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
      when(responseSpec.bodyToMono(ProductDetailDto.class)).thenReturn(Mono.error(ex));

      Mono<ProductDetailDto> result = apiClient.getProductDetail(productId);

      StepVerifier.create(result).expectError(WebClientResponseException.class).verify();
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle connection timeout for getSimilarProductIds")
    void shouldHandleConnectionTimeoutForSimilarIds() {
      String productId = "100";
      WebClientException timeoutException = new WebClientException("Connection timeout") {};

      when(webClient.get()).thenReturn(requestHeadersUriSpec);
      when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
      when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
      when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
      when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
          .thenReturn(Mono.error(timeoutException));

      Mono<List<String>> result = apiClient.getSimilarProductIds(productId);

      StepVerifier.create(result).expectError(ExternalApiException.class).verify();
    }

    @Test
    @DisplayName("Should verify URI template for getSimilarProductIds")
    void shouldVerifyUriTemplateForSimilarIds() {
      String productId = "ABC123";
      List<String> expectedIds = List.of("1");

      when(webClient.get()).thenReturn(requestHeadersUriSpec);
      when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
      when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
      when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
      when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
          .thenReturn(Mono.just(expectedIds));

      apiClient.getSimilarProductIds(productId);

      verify(requestHeadersUriSpec).uri("/{productId}/similarids", productId);
    }

    @Test
    @DisplayName("Should verify URI template for getProductDetail")
    void shouldVerifyUriTemplateForProductDetail() {
      String productId = "XYZ789";
      ProductDetailDto product =
          createProductDetailDto("1", "Test Product", BigDecimal.valueOf(99.99), true);

      when(webClient.get()).thenReturn(requestHeadersUriSpec);
      when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
      when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
      when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
      when(responseSpec.bodyToMono(ProductDetailDto.class)).thenReturn(Mono.just(product));

      apiClient.getProductDetail(productId);

      verify(requestHeadersUriSpec).uri("/{productId}", productId);
    }
  }
}
