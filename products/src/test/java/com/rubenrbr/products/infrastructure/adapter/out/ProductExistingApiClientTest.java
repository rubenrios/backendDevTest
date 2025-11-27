package com.rubenrbr.products.infrastructure.adapter.out;

import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetailDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.rubenrbr.products.domain.exception.ExternalApiException;
import com.rubenrbr.products.domain.exception.InvalidProductRequestException;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
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

  @Test
  void getSimilarProductIds_shouldReturnListOfIds_whenApiReturnsSuccessfully() {
    String productId = "1";
    List<String> expectedIds = Arrays.asList("2", "3", "4");

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
        .thenReturn(Mono.just(expectedIds));

    Optional<List<String>> result = apiClient.getSimilarProductIds(productId);

    assertThat(result).isPresent();
    assertThat(result.get()).hasSize(3);
    assertThat(result.get()).containsExactly("2", "3", "4");
    verify(webClient).get();
  }

  @Test
  void getSimilarProductIds_shouldReturnEmptyOptional_whenApiReturnsNull() {
    String productId = "1";

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.empty());

    Optional<List<String>> result = apiClient.getSimilarProductIds(productId);

    assertThat(result).isEmpty();
  }

  @Test
  void getSimilarProductIds_shouldThrowInvalidProductRequestException_when400Response() {
    String productId = "invalid";

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any()))
        .thenAnswer(
            invocation -> {
              throw new InvalidProductRequestException(productId);
            });

    assertThatThrownBy(() -> apiClient.getSimilarProductIds(productId))
        .isInstanceOf(InvalidProductRequestException.class);
  }

  @Test
  void getSimilarProductIds_shouldThrowExternalApiException_when5xxResponse() {
    String productId = "1";

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any()))
        .thenAnswer(
            invocation -> {
              throw new ExternalApiException();
            });

    assertThatThrownBy(() -> apiClient.getSimilarProductIds(productId))
        .isInstanceOf(ExternalApiException.class);
  }

  @Test
  void getSimilarProductIds_shouldThrowExternalApiException_onRuntimeException() {
    String productId = "1";

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object.class)))
        .thenThrow(new RuntimeException("Network error"));

    assertThatThrownBy(() -> apiClient.getSimilarProductIds(productId))
        .isInstanceOf(ExternalApiException.class);
  }

  @Test
  void getSimilarProductIds_shouldReturnEmptyList_whenApiReturnsEmptyList() {
    String productId = "1";
    List<String> emptyList = Arrays.asList();

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
        .thenReturn(Mono.just(emptyList));

    Optional<List<String>> result = apiClient.getSimilarProductIds(productId);

    assertThat(result).isPresent();
    assertThat(result.get()).isEmpty();
  }

  @Test
  void getProductDetail_shouldReturnProductDto_whenApiReturnsSuccessfully() {
    String productId = "1";
    ProductDetailDto expectedDto = createProductDetailDto("1", "Product1");

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(ProductDetailDto.class)).thenReturn(Mono.just(expectedDto));

    Optional<ProductDetailDto> result = apiClient.getProductDetail(productId);

    assertThat(result).isPresent();
    assertThat(result.get().getId()).isEqualTo("1");
    assertThat(result.get().getName()).isEqualTo("Product1");
    assertThat(result.get().getPrice()).isEqualTo(BigDecimal.ONE);
    verify(webClient).get();
  }

  @Test
  void getProductDetail_shouldReturnEmptyOptional_whenApiReturnsNull() {
    String productId = "1";

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(ProductDetailDto.class)).thenReturn(Mono.empty());

    Optional<ProductDetailDto> result = apiClient.getProductDetail(productId);

    assertThat(result).isEmpty();
  }

  @Test
  void getProductDetail_shouldReturnEmptyOptional_when404NotFound() {
    String productId = "999";
    WebClientResponseException notFoundException =
        WebClientResponseException.create(404, "Not Found", null, null, null);

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(ProductDetailDto.class)).thenThrow(notFoundException);

    Optional<ProductDetailDto> result = apiClient.getProductDetail(productId);

    assertThat(result).isEmpty();
  }

  @Test
  void getProductDetail_shouldThrowInvalidProductRequestException_when400Response() {
    String productId = "invalid";

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any()))
        .thenAnswer(
            invocation -> {
              throw new InvalidProductRequestException(productId);
            });

    assertThatThrownBy(() -> apiClient.getProductDetail(productId))
        .isInstanceOf(InvalidProductRequestException.class);
  }

  @Test
  void getProductDetail_shouldThrowExternalApiException_when5xxResponse() {
    String productId = "1";

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any()))
        .thenAnswer(
            invocation -> {
              throw new ExternalApiException();
            });

    assertThatThrownBy(() -> apiClient.getProductDetail(productId))
        .isInstanceOf(ExternalApiException.class);
  }

  @Test
  void getProductDetail_shouldHandleCompleteProductData() {
    String productId = "1";
    ProductDetailDto expectedDto = createProductDetailDto("1", "Product1");

    when(webClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    when(responseSpec.bodyToMono(ProductDetailDto.class)).thenReturn(Mono.just(expectedDto));

    Optional<ProductDetailDto> result = apiClient.getProductDetail(productId);

    assertThat(result).isPresent();
    ProductDetailDto dto = result.get();
    assertThat(dto.getId()).isEqualTo("1");
    assertThat(dto.getName()).isEqualTo("Product1");
    assertThat(dto.getPrice()).isEqualTo(BigDecimal.ONE);
    assertThat(dto.getAvailability()).isTrue();
  }
}
