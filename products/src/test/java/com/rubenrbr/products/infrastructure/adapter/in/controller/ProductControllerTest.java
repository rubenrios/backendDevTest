package com.rubenrbr.products.infrastructure.adapter.in.controller;

import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetail;
import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetailDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;

import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.domain.port.in.ProductService;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;
import com.rubenrbr.products.infrastructure.rest.mapper.ProductResponseMapper;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Unit Tests")
class ProductControllerTest {

  @Mock private ProductService productService;

  @Mock private ProductResponseMapper mapper;

  @Mock private ServerWebExchange exchange;

  @InjectMocks private ProductController productController;

  private ProductDetail productDetail1;
  private ProductDetail productDetail2;
  private ProductDetailDto productDetailDto1;
  private ProductDetailDto productDetailDto2;
  private Set<ProductDetail> productDetails;
  private Set<ProductDetailDto> productDetailDtos;

  @BeforeEach
  void setUp() {
    productDetail1 = createProductDetail("1", "Product 1", BigDecimal.valueOf(10.99), true);

    productDetail2 = createProductDetail("2", "Product 2", BigDecimal.valueOf(20.99), true);

    productDetailDto1 = createProductDetailDto("1", "Product 1", BigDecimal.valueOf(10.99), true);

    productDetailDto2 = createProductDetailDto("2", "Product 2", BigDecimal.valueOf(20.99), true);

    productDetails = Set.of(productDetail1, productDetail2);
    productDetailDtos = Set.of(productDetailDto1, productDetailDto2);
  }

  @Test
  @DisplayName("Should return 200 OK with similar products")
  void shouldReturn200WithSimilarProducts() {
    String productId = "100";

    when(productService.getSimilarProducts(productId)).thenReturn(Mono.just(productDetails));
    when(mapper.productDetailToProductDetailDto(productDetails)).thenReturn(productDetailDtos);

    Mono<ResponseEntity<Flux<ProductDetailDto>>> result =
        productController.getProductSimilar(productId, exchange);

    StepVerifier.create(result)
        .assertNext(
            response -> {
              assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
              assertThat(response.getBody()).isNotNull();

              StepVerifier.create(response.getBody()).expectNextCount(2).verifyComplete();
            })
        .verifyComplete();

    verify(productService).getSimilarProducts(productId);
    verify(mapper).productDetailToProductDetailDto(productDetails);
  }

  @Test
  @DisplayName("Should return 200 OK with empty flux when no similar products exist")
  void shouldReturn200WithEmptyFluxWhenNoSimilarProducts() {
    String productId = "100";
    Set<ProductDetail> emptySet = Set.of();
    Set<ProductDetailDto> emptyDtoSet = Set.of();

    when(productService.getSimilarProducts(productId)).thenReturn(Mono.just(emptySet));
    when(mapper.productDetailToProductDetailDto(emptySet)).thenReturn(emptyDtoSet);

    Mono<ResponseEntity<Flux<ProductDetailDto>>> result =
        productController.getProductSimilar(productId, exchange);

    StepVerifier.create(result)
        .assertNext(
            response -> {
              assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
              assertThat(response.getBody()).isNotNull();

              StepVerifier.create(response.getBody()).verifyComplete();
            })
        .verifyComplete();

    verify(productService).getSimilarProducts(productId);
    verify(mapper).productDetailToProductDetailDto(emptySet);
  }

  @Test
  @DisplayName("Should propagate error from service layer")
  void shouldPropagateErrorFromServiceLayer() {
    String productId = "100";
    RuntimeException exception = new RuntimeException("Service error");

    when(productService.getSimilarProducts(productId)).thenReturn(Mono.error(exception));

    Mono<ResponseEntity<Flux<ProductDetailDto>>> result =
        productController.getProductSimilar(productId, exchange);

    StepVerifier.create(result)
        .expectErrorMatches(
            e -> e instanceof RuntimeException && e.getMessage().equals("Service error"))
        .verify();

    verify(productService).getSimilarProducts(productId);
  }

  @Test
  @DisplayName("Should handle mapper returning single product")
  void shouldHandleMapperReturningSingleProduct() {
    String productId = "100";
    Set<ProductDetail> singleProduct = Set.of(productDetail1);
    Set<ProductDetailDto> singleDto = Set.of(productDetailDto1);

    when(productService.getSimilarProducts(productId)).thenReturn(Mono.just(singleProduct));
    when(mapper.productDetailToProductDetailDto(singleProduct)).thenReturn(singleDto);

    Mono<ResponseEntity<Flux<ProductDetailDto>>> result =
        productController.getProductSimilar(productId, exchange);

    StepVerifier.create(result)
        .assertNext(
            response -> {
              assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

              StepVerifier.create(response.getBody())
                  .assertNext(
                      dto -> {
                        assertThat(dto.getId()).isEqualTo("1");
                        assertThat(dto.getName()).isEqualTo("Product 1");
                      })
                  .verifyComplete();
            })
        .verifyComplete();
  }

  @Test
  @DisplayName("Should verify all products are mapped correctly")
  void shouldVerifyAllProductsAreMappedCorrectly() {
    String productId = "100";

    when(productService.getSimilarProducts(productId)).thenReturn(Mono.just(productDetails));
    when(mapper.productDetailToProductDetailDto(anySet())).thenReturn(productDetailDtos);

    Mono<ResponseEntity<Flux<ProductDetailDto>>> result =
        productController.getProductSimilar(productId, exchange);

    StepVerifier.create(result)
        .assertNext(
            response -> {
              StepVerifier.create(response.getBody())
                  .recordWith(java.util.ArrayList::new)
                  .expectNextCount(2)
                  .consumeRecordedWith(
                      products -> {
                        assertThat(products).hasSize(2);
                        assertThat(products)
                            .extracting(ProductDetailDto::getId)
                            .containsExactlyInAnyOrder("1", "2");
                      })
                  .verifyComplete();
            })
        .verifyComplete();

    verify(productService).getSimilarProducts(productId);
    verify(mapper).productDetailToProductDetailDto(anySet());
  }
}
