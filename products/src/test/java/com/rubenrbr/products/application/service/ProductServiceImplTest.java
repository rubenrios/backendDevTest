package com.rubenrbr.products.application.service;

import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rubenrbr.products.domain.exception.ProductNotFoundException;
import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.domain.port.out.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  @Mock private ProductRepository productRepository;

  @InjectMocks private ProductServiceImpl productService;

  @Test
  void getSimilarProducts_shouldReturnAllProducts_whenAllIdsExist() {
    String productId = "1";
    List<String> similarIds = Arrays.asList("2", "3", "4");

    ProductDetail product2 = createProductDetail("2", "Product2");
    ProductDetail product3 = createProductDetail("3", "Product3");
    ProductDetail product4 = createProductDetail("4", "Product4");

    when(productRepository.getSimilarIds(productId)).thenReturn(similarIds);
    when(productRepository.getProductDetail("2")).thenReturn(product2);
    when(productRepository.getProductDetail("3")).thenReturn(product3);
    when(productRepository.getProductDetail("4")).thenReturn(product4);

    Set<ProductDetail> result = productService.getSimilarProducts(productId);

    assertThat(result).hasSize(3);
    assertThat(result).containsExactlyInAnyOrder(product2, product3, product4);
    verify(productRepository).getSimilarIds(productId);
    verify(productRepository).getProductDetail("2");
    verify(productRepository).getProductDetail("3");
    verify(productRepository).getProductDetail("4");
  }

  @Test
  void getSimilarProducts_shouldReturnEmptySet_whenNoSimilarIdsExist() {
    String productId = "1";
    List<String> similarIds = Collections.emptyList();

    when(productRepository.getSimilarIds(productId)).thenReturn(similarIds);

    Set<ProductDetail> result = productService.getSimilarProducts(productId);

    assertThat(result).isEmpty();
    verify(productRepository).getSimilarIds(productId);
    verify(productRepository, times(0)).getProductDetail(anyString());
  }

  @Test
  void getSimilarProducts_shouldFilterOutNulls_whenSomeProductsNotFound() {
    String productId = "1";
    List<String> similarIds = Arrays.asList("2", "3", "4");

    ProductDetail product2 = createProductDetail("2", "Product2");
    ProductDetail product4 = createProductDetail("4", "Product4");

    when(productRepository.getSimilarIds(productId)).thenReturn(similarIds);
    when(productRepository.getProductDetail("2")).thenReturn(product2);
    when(productRepository.getProductDetail("3"))
        .thenThrow(new ProductNotFoundException("Product 3 not found"));
    when(productRepository.getProductDetail("4")).thenReturn(product4);

    Set<ProductDetail> result = productService.getSimilarProducts(productId);

    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyInAnyOrder(product2, product4);
    verify(productRepository).getSimilarIds(productId);
    verify(productRepository).getProductDetail("2");
    verify(productRepository).getProductDetail("3");
    verify(productRepository).getProductDetail("4");
  }

  @Test
  void getSimilarProducts_shouldReturnEmptySet_whenAllProductsNotFound() {
    String productId = "1";
    List<String> similarIds = Arrays.asList("2", "3");

    when(productRepository.getSimilarIds(productId)).thenReturn(similarIds);
    when(productRepository.getProductDetail("2"))
        .thenThrow(new ProductNotFoundException("Product 2 not found"));
    when(productRepository.getProductDetail("3"))
        .thenThrow(new ProductNotFoundException("Product 3 not found"));

    Set<ProductDetail> result = productService.getSimilarProducts(productId);

    assertThat(result).isEmpty();
    verify(productRepository).getSimilarIds(productId);
    verify(productRepository).getProductDetail("2");
    verify(productRepository).getProductDetail("3");
  }

  @Test
  void getSimilarProducts_shouldReturnSetWithoutDuplicates_whenSimilarIdsContainDuplicates() {
    String productId = "1";
    List<String> similarIds = Arrays.asList("2", "2", "3");

    ProductDetail product2 = createProductDetail("2", "Product 2");
    ProductDetail product3 = createProductDetail("3", "Product 3");

    when(productRepository.getSimilarIds(productId)).thenReturn(similarIds);
    when(productRepository.getProductDetail("2")).thenReturn(product2);
    when(productRepository.getProductDetail("3")).thenReturn(product3);

    Set<ProductDetail> result = productService.getSimilarProducts(productId);

    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyInAnyOrder(product2, product3);
  }
}
