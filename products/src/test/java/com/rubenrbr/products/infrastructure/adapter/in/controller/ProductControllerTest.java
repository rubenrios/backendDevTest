package com.rubenrbr.products.infrastructure.adapter.in.controller;

import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetail;
import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetailDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.domain.port.in.ProductService;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;
import com.rubenrbr.products.infrastructure.rest.mapper.ProductResponseMapper;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

  @Mock private ProductService productService;

  @Mock private ProductResponseMapper mapper;

  @InjectMocks private ProductController productController;

  @Test
  void getProductSimilar_shouldReturnOkWithProducts_whenProductsExist() {
    String productId = "1";

    ProductDetail product1 = createProductDetail("2", "Product2");
    ProductDetail product2 = createProductDetail("3", "Product3");
    Set<ProductDetail> products = Set.of(product1, product2);

    ProductDetailDto dto1 = createProductDetailDto("2", "Product2");
    ProductDetailDto dto2 = createProductDetailDto("3", "Product3");
    Set<ProductDetailDto> productDtos = Set.of(dto1, dto2);

    when(productService.getSimilarProducts(productId)).thenReturn(products);
    when(mapper.productDetailToProductDetailDto(products)).thenReturn(productDtos);

    ResponseEntity<Set<ProductDetailDto>> response = productController.getProductSimilar(productId);

    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).hasSize(2);
    assertThat(response.getBody()).containsExactlyInAnyOrder(dto1, dto2);

    verify(productService).getSimilarProducts(productId);
    verify(mapper).productDetailToProductDetailDto(products);
  }

  @Test
  void getProductSimilar_shouldReturnOkWithEmptySet_whenNoSimilarProducts() {
    String productId = "1";
    Set<ProductDetail> emptyProducts = Collections.emptySet();
    Set<ProductDetailDto> emptyDtos = Collections.emptySet();

    when(productService.getSimilarProducts(productId)).thenReturn(emptyProducts);
    when(mapper.productDetailToProductDetailDto(emptyProducts)).thenReturn(emptyDtos);

    ResponseEntity<Set<ProductDetailDto>> response = productController.getProductSimilar(productId);

    assertThat(response.getStatusCodeValue()).isEqualTo(200);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody()).isEmpty();

    verify(productService).getSimilarProducts(productId);
    verify(mapper).productDetailToProductDetailDto(emptyProducts);
  }
}
