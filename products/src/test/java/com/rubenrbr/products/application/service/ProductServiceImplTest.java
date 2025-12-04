package com.rubenrbr.products.application.service;

import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rubenrbr.products.domain.exception.ProductNotFoundException;
import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.domain.port.out.ProductRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Tests")
class ProductServiceImplTest {

  @Mock private ProductRepository productRepository;

  @InjectMocks private ProductServiceImpl productService;

  private ProductDetail productDetail1;
  private ProductDetail productDetail2;
  private ProductDetail productDetail3;

  @BeforeEach
  void setUp() {
    productDetail1 = createProductDetail("1", "Product 1", BigDecimal.valueOf(10.99), true);

    productDetail2 = createProductDetail("2", "Product 2", BigDecimal.valueOf(20.99), true);

    productDetail3 = createProductDetail("3", "Product 3", BigDecimal.valueOf(30.99), true);
  }

  @Test
  @DisplayName("Should return all similar products when all exist")
  void shouldReturnAllSimilarProductsWhenAllExist() {
    String productId = "100";
    List<String> similarIds = List.of("1", "2", "3");

    when(productRepository.getSimilarIds(productId)).thenReturn(Mono.just(similarIds));
    when(productRepository.getProductDetail("1")).thenReturn(Mono.just(productDetail1));
    when(productRepository.getProductDetail("2")).thenReturn(Mono.just(productDetail2));
    when(productRepository.getProductDetail("3")).thenReturn(Mono.just(productDetail3));

    StepVerifier.create(productService.getSimilarProducts(productId))
        .assertNext(
            products -> {
              assertThat(products).hasSize(3);
              assertThat(products)
                  .containsExactlyInAnyOrder(productDetail1, productDetail2, productDetail3);
            })
        .verifyComplete();

    verify(productRepository).getSimilarIds(productId);
    verify(productRepository).getProductDetail("1");
    verify(productRepository).getProductDetail("2");
    verify(productRepository).getProductDetail("3");
  }

  @Test
  @DisplayName("Should return empty set when no similar products exist")
  void shouldReturnEmptySetWhenNoSimilarProductsExist() {
    String productId = "100";
    List<String> similarIds = List.of();

    when(productRepository.getSimilarIds(productId)).thenReturn(Mono.just(similarIds));

    StepVerifier.create(productService.getSimilarProducts(productId))
        .assertNext(products -> assertThat(products).isEmpty())
        .verifyComplete();

    verify(productRepository).getSimilarIds(productId);
    verify(productRepository, never()).getProductDetail(anyString());
  }

  @Test
  @DisplayName("Should skip products that throw ProductNotFoundException")
  void shouldSkipProductsThatThrowProductNotFoundException() {
    String productId = "100";
    List<String> similarIds = List.of("1", "2", "3");

    when(productRepository.getSimilarIds(productId)).thenReturn(Mono.just(similarIds));
    when(productRepository.getProductDetail("1")).thenReturn(Mono.just(productDetail1));
    when(productRepository.getProductDetail("2"))
        .thenReturn(Mono.error(new ProductNotFoundException("Product 2 not found")));
    when(productRepository.getProductDetail("3")).thenReturn(Mono.just(productDetail3));

    StepVerifier.create(productService.getSimilarProducts(productId))
        .assertNext(
            products -> {
              assertThat(products).hasSize(2);
              assertThat(products).containsExactlyInAnyOrder(productDetail1, productDetail3);
              assertThat(products).doesNotContain(productDetail2);
            })
        .verifyComplete();

    verify(productRepository).getSimilarIds(productId);
    verify(productRepository, times(3)).getProductDetail(anyString());
  }

  @Test
  @DisplayName("Should return empty set when all products throw ProductNotFoundException")
  void shouldReturnEmptySetWhenAllProductsThrowProductNotFoundException() {
    String productId = "100";
    List<String> similarIds = List.of("1", "2", "3");

    when(productRepository.getSimilarIds(productId)).thenReturn(Mono.just(similarIds));
    when(productRepository.getProductDetail("1"))
        .thenReturn(Mono.error(new ProductNotFoundException("Product 1 not found")));
    when(productRepository.getProductDetail("2"))
        .thenReturn(Mono.error(new ProductNotFoundException("Product 2 not found")));
    when(productRepository.getProductDetail("3"))
        .thenReturn(Mono.error(new ProductNotFoundException("Product 3 not found")));

    StepVerifier.create(productService.getSimilarProducts(productId))
        .assertNext(products -> assertThat(products).isEmpty())
        .verifyComplete();

    verify(productRepository).getSimilarIds(productId);
    verify(productRepository, times(3)).getProductDetail(anyString());
  }

  @Test
  @DisplayName("Should propagate error when getSimilarIds fails")
  void shouldPropagateErrorWhenGetSimilarIdsFails() {
    String productId = "100";
    RuntimeException exception = new RuntimeException("Database error");

    when(productRepository.getSimilarIds(productId)).thenReturn(Mono.error(exception));

    StepVerifier.create(productService.getSimilarProducts(productId))
        .expectErrorMatches(
            e -> e instanceof RuntimeException && e.getMessage().equals("Database error"))
        .verify();

    verify(productRepository).getSimilarIds(productId);
    verify(productRepository, never()).getProductDetail(anyString());
  }

  @Test
  @DisplayName("Should propagate non-ProductNotFoundException errors from getProductDetail")
  void shouldPropagateNonProductNotFoundExceptionErrors() {
    String productId = "100";
    List<String> similarIds = List.of("1", "2");
    RuntimeException exception = new RuntimeException("Unexpected error");

    when(productRepository.getSimilarIds(productId)).thenReturn(Mono.just(similarIds));
    when(productRepository.getProductDetail("1")).thenReturn(Mono.just(productDetail1));
    when(productRepository.getProductDetail("2")).thenReturn(Mono.error(exception));

    StepVerifier.create(productService.getSimilarProducts(productId))
        .expectErrorMatches(
            e -> e instanceof RuntimeException && e.getMessage().equals("Unexpected error"))
        .verify();

    verify(productRepository).getSimilarIds(productId);
  }

  @Test
  @DisplayName("Should handle single similar product")
  void shouldHandleSingleSimilarProduct() {
    String productId = "100";
    List<String> similarIds = List.of("1");

    when(productRepository.getSimilarIds(productId)).thenReturn(Mono.just(similarIds));
    when(productRepository.getProductDetail("1")).thenReturn(Mono.just(productDetail1));

    StepVerifier.create(productService.getSimilarProducts(productId))
        .assertNext(
            products -> {
              assertThat(products).hasSize(1);
              assertThat(products).contains(productDetail1);
            })
        .verifyComplete();
  }

  @Test
  @DisplayName("Should handle duplicate product IDs and return unique products")
  void shouldHandleDuplicateProductIds() {
    String productId = "100";
    List<String> similarIds = List.of("1", "1", "2");

    when(productRepository.getSimilarIds(productId)).thenReturn(Mono.just(similarIds));
    when(productRepository.getProductDetail("1")).thenReturn(Mono.just(productDetail1));
    when(productRepository.getProductDetail("2")).thenReturn(Mono.just(productDetail2));

    StepVerifier.create(productService.getSimilarProducts(productId))
        .assertNext(
            products -> {
              assertThat(products).hasSize(2);
              assertThat(products).containsExactlyInAnyOrder(productDetail1, productDetail2);
            })
        .verifyComplete();

    verify(productRepository, times(2)).getProductDetail("1");
    verify(productRepository).getProductDetail("2");
  }
}
