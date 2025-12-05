package com.rubenrbr.products.infrastructure.adapter.out;

import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetail;
import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetailDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rubenrbr.products.domain.exception.ExternalApiException;
import com.rubenrbr.products.domain.exception.InvalidProductRequestException;
import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductRepositoryAdapter Unit Tests")
class ProductRepositoryAdapterTest {

  @Mock private ProductExistingApiClient productExistingApiClient;

  @Mock private ProductMapper productMapper;

  @InjectMocks private ProductRepositoryAdapter productRepositoryAdapter;

  private ProductDetailDto productDetailDto;
  private ProductDetail productDetail;

  @BeforeEach
  void setUp() {
    productDetailDto = createProductDetailDto("1", "Test Product", BigDecimal.valueOf(99.99), true);

    productDetail = createProductDetail("1", "Test Product", BigDecimal.valueOf(99.99), true);
  }

  @Nested
  @DisplayName("getProductDetail Tests")
  class GetProductDetailTests {

    @Test
    @DisplayName("Should return product detail when API returns data")
    void shouldReturnProductDetailWhenApiReturnsData() {
      String productId = "1";

      when(productExistingApiClient.getProductDetail(productId))
          .thenReturn(Mono.just(productDetailDto));
      when(productMapper.productDetailDtoToProductDetail(productDetailDto))
          .thenReturn(productDetail);

      Mono<ProductDetail> result = productRepositoryAdapter.getProductDetail(productId);

      StepVerifier.create(result)
          .assertNext(
              detail -> {
                assertThat(detail).isNotNull();
                assertThat(detail.id()).isEqualTo("1");
                assertThat(detail.name()).isEqualTo("Test Product");
                assertThat(detail.price()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
                assertThat(detail.availability()).isTrue();
              })
          .verifyComplete();

      verify(productExistingApiClient).getProductDetail(productId);
      verify(productMapper).productDetailDtoToProductDetail(productDetailDto);
    }

    @Test
    @DisplayName("Should propagate InvalidProductRequestException from API client")
    void shouldPropagateInvalidProductRequestException() {
      String productId = "invalid";
      InvalidProductRequestException exception = new InvalidProductRequestException(productId);

      when(productExistingApiClient.getProductDetail(productId)).thenReturn(Mono.error(exception));

      Mono<ProductDetail> result = productRepositoryAdapter.getProductDetail(productId);

      StepVerifier.create(result)
          .expectErrorMatches(
              error ->
                  error instanceof InvalidProductRequestException
                      && error.getMessage().contains(productId))
          .verify();

      verify(productExistingApiClient).getProductDetail(productId);
      verify(productMapper, never()).productDetailDtoToProductDetail(any());
    }

    @Test
    @DisplayName("Should propagate ExternalApiException from API client")
    void shouldPropagateExternalApiException() {
      String productId = "1";
      ExternalApiException exception = new ExternalApiException();

      when(productExistingApiClient.getProductDetail(productId)).thenReturn(Mono.error(exception));

      Mono<ProductDetail> result = productRepositoryAdapter.getProductDetail(productId);

      StepVerifier.create(result).expectError(ExternalApiException.class).verify();

      verify(productExistingApiClient).getProductDetail(productId);
      verify(productMapper, never()).productDetailDtoToProductDetail(any());
    }

    @Test
    @DisplayName("Should handle mapper throwing exception")
    void shouldHandleMapperThrowingException() {
      String productId = "1";
      RuntimeException mapperException = new RuntimeException("Mapping error");

      when(productExistingApiClient.getProductDetail(productId))
          .thenReturn(Mono.just(productDetailDto));
      when(productMapper.productDetailDtoToProductDetail(productDetailDto))
          .thenThrow(mapperException);

      Mono<ProductDetail> result = productRepositoryAdapter.getProductDetail(productId);

      StepVerifier.create(result)
          .expectErrorMatches(
              error ->
                  error instanceof RuntimeException && error.getMessage().equals("Mapping error"))
          .verify();

      verify(productExistingApiClient).getProductDetail(productId);
      verify(productMapper).productDetailDtoToProductDetail(productDetailDto);
    }
  }

  @Nested
  @DisplayName("getSimilarIds Tests")
  class GetSimilarIdsTests {

    @Test
    @DisplayName("Should return list of similar IDs when API returns data")
    void shouldReturnListOfSimilarIds() {
      String productId = "100";
      List<String> expectedIds = List.of("1", "2", "3");

      when(productExistingApiClient.getSimilarProductIds(productId))
          .thenReturn(Mono.just(expectedIds));

      Mono<List<String>> result = productRepositoryAdapter.getSimilarIds(productId);

      StepVerifier.create(result)
          .assertNext(
              ids -> {
                assertThat(ids).hasSize(3);
                assertThat(ids).containsExactly("1", "2", "3");
              })
          .verifyComplete();

      verify(productExistingApiClient).getSimilarProductIds(productId);
    }

    @Test
    @DisplayName("Should return empty list when API returns empty list")
    void shouldReturnEmptyListWhenApiReturnsEmptyList() {
      String productId = "100";
      List<String> emptyList = Collections.emptyList();

      when(productExistingApiClient.getSimilarProductIds(productId))
          .thenReturn(Mono.just(emptyList));

      Mono<List<String>> result = productRepositoryAdapter.getSimilarIds(productId);

      StepVerifier.create(result).assertNext(ids -> assertThat(ids).isEmpty()).verifyComplete();

      verify(productExistingApiClient).getSimilarProductIds(productId);
    }

    @Test
    @DisplayName("Should propagate InvalidProductRequestException from API client")
    void shouldPropagateInvalidProductRequestException() {
      String productId = "invalid";
      InvalidProductRequestException exception = new InvalidProductRequestException(productId);

      when(productExistingApiClient.getSimilarProductIds(productId))
          .thenReturn(Mono.error(exception));

      Mono<List<String>> result = productRepositoryAdapter.getSimilarIds(productId);

      StepVerifier.create(result)
          .expectErrorMatches(
              error ->
                  error instanceof InvalidProductRequestException
                      && error.getMessage().contains(productId))
          .verify();

      verify(productExistingApiClient).getSimilarProductIds(productId);
    }

    @Test
    @DisplayName("Should propagate ExternalApiException from API client")
    void shouldPropagateExternalApiException() {
      String productId = "100";
      ExternalApiException exception = new ExternalApiException();

      when(productExistingApiClient.getSimilarProductIds(productId))
          .thenReturn(Mono.error(exception));

      Mono<List<String>> result = productRepositoryAdapter.getSimilarIds(productId);

      StepVerifier.create(result).expectError(ExternalApiException.class).verify();

      verify(productExistingApiClient).getSimilarProductIds(productId);
    }

    @Test
    @DisplayName("Should handle single similar ID")
    void shouldHandleSingleSimilarId() {
      String productId = "100";
      List<String> singleId = List.of("1");

      when(productExistingApiClient.getSimilarProductIds(productId))
          .thenReturn(Mono.just(singleId));

      Mono<List<String>> result = productRepositoryAdapter.getSimilarIds(productId);

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
  @DisplayName("Integration Between Methods Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should handle workflow: get similar IDs then get product details")
    void shouldHandleCompleteWorkflow() {
      String mainProductId = "100";
      String similarProductId = "1";
      List<String> similarIds = List.of(similarProductId);

      when(productExistingApiClient.getSimilarProductIds(mainProductId))
          .thenReturn(Mono.just(similarIds));
      when(productExistingApiClient.getProductDetail(similarProductId))
          .thenReturn(Mono.just(productDetailDto));
      when(productMapper.productDetailDtoToProductDetail(productDetailDto))
          .thenReturn(productDetail);

      Mono<List<String>> idsResult = productRepositoryAdapter.getSimilarIds(mainProductId);

      StepVerifier.create(idsResult)
          .assertNext(ids -> assertThat(ids).containsExactly(similarProductId))
          .verifyComplete();

      Mono<ProductDetail> detailResult =
          productRepositoryAdapter.getProductDetail(similarProductId);

      StepVerifier.create(detailResult)
          .assertNext(
              detail -> {
                assertThat(detail.id()).isEqualTo(similarProductId);
                assertThat(detail.name()).isEqualTo("Test Product");
              })
          .verifyComplete();

      verify(productExistingApiClient).getSimilarProductIds(mainProductId);
      verify(productExistingApiClient).getProductDetail(similarProductId);
      verify(productMapper).productDetailDtoToProductDetail(productDetailDto);
    }

    @Test
    @DisplayName("Should verify adapter delegates correctly to API client")
    void shouldVerifyAdapterDelegatesCorrectly() {
      String productId = "123";

      when(productExistingApiClient.getProductDetail(anyString()))
          .thenReturn(Mono.just(productDetailDto));
      when(productExistingApiClient.getSimilarProductIds(anyString()))
          .thenReturn(Mono.just(List.of("1", "2")));
      when(productMapper.productDetailDtoToProductDetail(any())).thenReturn(productDetail);

      productRepositoryAdapter.getProductDetail(productId).block();
      productRepositoryAdapter.getSimilarIds(productId).block();

      verify(productExistingApiClient).getProductDetail(productId);
      verify(productExistingApiClient).getSimilarProductIds(productId);
      verify(productMapper).productDetailDtoToProductDetail(productDetailDto);
    }
  }
}
