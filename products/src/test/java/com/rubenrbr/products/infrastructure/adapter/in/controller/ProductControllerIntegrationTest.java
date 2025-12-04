package com.rubenrbr.products.infrastructure.adapter.in.controller;

import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetail;
import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetailDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.rubenrbr.products.domain.exception.ProductNotFoundException;
import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.domain.port.in.ProductService;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;
import com.rubenrbr.products.infrastructure.rest.mapper.ProductResponseMapper;

import reactor.core.publisher.Mono;

@WebFluxTest(ProductController.class)
@DisplayName("ProductController Integration Tests")
class ProductControllerIntegrationTest {

  @Autowired private WebTestClient webTestClient;

  @MockBean private ProductService productService;

  @MockBean private ProductResponseMapper mapper;

  @Test
  @DisplayName("GET /product/{productId}/similar - Should return 200 with similar products")
  void getSimilarProducts_shouldReturn200WithProducts() {
    String productId = "100";

    ProductDetail product1 = createProductDetail("1", "Product 1", BigDecimal.valueOf(10.99), true);

    ProductDetail product2 = createProductDetail("2", "Product 2", BigDecimal.valueOf(20.99), true);

    Set<ProductDetail> products = Set.of(product1, product2);

    ProductDetailDto dto1 =
        createProductDetailDto("1", "Product 1", BigDecimal.valueOf(10.99), true);

    ProductDetailDto dto2 =
        createProductDetailDto("2", "Product 2", BigDecimal.valueOf(20.99), true);

    Set<ProductDetailDto> dtos = Set.of(dto1, dto2);

    when(productService.getSimilarProducts(productId)).thenReturn(Mono.just(products));
    when(mapper.productDetailToProductDetailDto(products)).thenReturn(dtos);

    webTestClient
        .get()
        .uri("/product/{productId}/similar", productId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(ProductDetailDto.class)
        .hasSize(2)
        .consumeWith(
            response -> {
              var body = response.getResponseBody();
              assertThat(body).isNotNull();
              assertThat(body)
                  .extracting(ProductDetailDto::getId)
                  .containsExactlyInAnyOrder("1", "2");
              assertThat(body)
                  .extracting(ProductDetailDto::getName)
                  .containsExactlyInAnyOrder("Product 1", "Product 2");
            });
  }

  @Test
  @DisplayName(
      "GET /product/{productId}/similar - Should return 200 with empty array when no similar products")
  void getSimilarProducts_shouldReturn200WithEmptyArray() {
    String productId = "100";
    Set<ProductDetail> emptySet = Set.of();
    Set<ProductDetailDto> emptyDtoSet = Set.of();

    when(productService.getSimilarProducts(productId)).thenReturn(Mono.just(emptySet));
    when(mapper.productDetailToProductDetailDto(emptySet)).thenReturn(emptyDtoSet);

    webTestClient
        .get()
        .uri("/product/{productId}/similar", productId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBodyList(ProductDetailDto.class)
        .hasSize(0);
  }

  @Test
  @DisplayName("GET /product/{productId}/similar - Should return 404 when product not found")
  void getSimilarProducts_shouldReturn404WhenProductNotFound() {
    String productId = "999";

    when(productService.getSimilarProducts(productId))
        .thenReturn(Mono.error(new ProductNotFoundException("Product not found")));

    webTestClient
        .get()
        .uri("/product/{productId}/similar", productId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  @DisplayName("GET /product/{productId}/similar - Should return 500 on internal server error")
  void getSimilarProducts_shouldReturn500OnInternalError() {
    String productId = "100";

    when(productService.getSimilarProducts(productId))
        .thenReturn(Mono.error(new RuntimeException("Internal error")));

    webTestClient
        .get()
        .uri("/product/{productId}/similar", productId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .is5xxServerError();
  }

  @Test
  @DisplayName("GET /product/{productId}/similar - Should validate product details structure")
  void getSimilarProducts_shouldValidateProductDetailsStructure() {
    String productId = "100";

    ProductDetail product =
        createProductDetail("1", "Test Product", BigDecimal.valueOf(99.99), true);

    ProductDetailDto dto =
        createProductDetailDto("1", "Test Product", BigDecimal.valueOf(99.99), true);

    when(productService.getSimilarProducts(productId)).thenReturn(Mono.just(Set.of(product)));
    when(mapper.productDetailToProductDetailDto(Set.of(product))).thenReturn(Set.of(dto));

    webTestClient
        .get()
        .uri("/product/{productId}/similar", productId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$[0].id")
        .isEqualTo("1")
        .jsonPath("$[0].name")
        .isEqualTo("Test Product")
        .jsonPath("$[0].price")
        .isEqualTo(99.99)
        .jsonPath("$[0].availability")
        .isEqualTo(true);
  }

  @Test
  @DisplayName("GET /product/{productId}/similar - Should handle special characters in productId")
  void getSimilarProducts_shouldHandleSpecialCharactersInProductId() {
    String productId = "ABC-123";
    Set<ProductDetail> emptySet = Set.of();
    Set<ProductDetailDto> emptyDtoSet = Set.of();

    when(productService.getSimilarProducts(productId)).thenReturn(Mono.just(emptySet));
    when(mapper.productDetailToProductDetailDto(emptySet)).thenReturn(emptyDtoSet);

    webTestClient
        .get()
        .uri("/product/{productId}/similar", productId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk();
  }

  @Test
  @DisplayName("GET /product/{productId}/similar - Should accept different media types")
  void getSimilarProducts_shouldAcceptDifferentMediaTypes() {
    String productId = "100";
    Set<ProductDetail> emptySet = Set.of();
    Set<ProductDetailDto> emptyDtoSet = Set.of();

    when(productService.getSimilarProducts(anyString())).thenReturn(Mono.just(emptySet));
    when(mapper.productDetailToProductDetailDto(emptySet)).thenReturn(emptyDtoSet);

    webTestClient
        .get()
        .uri("/product/{productId}/similar", productId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON);
  }

  @Test
  @DisplayName(
      "GET /product/{productId}/similar - Should verify response contains all expected fields")
  void getSimilarProducts_shouldVerifyAllExpectedFields() {
    String productId = "100";

    ProductDetail product =
        createProductDetail("1", "Complete Product", BigDecimal.valueOf(40.99), true);

    ProductDetailDto dto =
        createProductDetailDto("1", "Complete Product", BigDecimal.valueOf(40.99), true);

    when(productService.getSimilarProducts(productId)).thenReturn(Mono.just(Set.of(product)));
    when(mapper.productDetailToProductDetailDto(Set.of(product))).thenReturn(Set.of(dto));

    webTestClient
        .get()
        .uri("/product/{productId}/similar", productId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody()
        .jsonPath("$[0].id")
        .exists()
        .jsonPath("$[0].name")
        .exists()
        .jsonPath("$[0].price")
        .exists()
        .jsonPath("$[0].availability")
        .exists();
  }
}
