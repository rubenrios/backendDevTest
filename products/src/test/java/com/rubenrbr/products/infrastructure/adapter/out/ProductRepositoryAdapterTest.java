package com.rubenrbr.products.infrastructure.adapter.out;

import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetail;
import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetailDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rubenrbr.products.domain.exception.ProductNotFoundException;
import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;

@ExtendWith(MockitoExtension.class)
class ProductRepositoryAdapterTest {

  @Mock private ProductExistingApiClient productExistingApiClient;

  @Mock private ProductMapper productMapper;

  @InjectMocks private ProductRepositoryAdapter productRepositoryAdapter;

  @Test
  void getProductDetail_shouldReturnMappedProduct_whenProductExists() {
    String productId = "1";
    ProductDetailDto dto = createProductDetailDto("1", "Product1");
    ProductDetail expectedProduct = createProductDetail("1", "Product1");

    when(productExistingApiClient.getProductDetail(productId)).thenReturn(Optional.of(dto));
    when(productMapper.productDetailDtoToProductDetail(dto)).thenReturn(expectedProduct);

    ProductDetail result = productRepositoryAdapter.getProductDetail(productId);

    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo("1");
    assertThat(result.name()).isEqualTo("Product1");
    assertThat(result.price()).isEqualTo(BigDecimal.ONE);
    assertThat(result.availability()).isTrue();

    verify(productExistingApiClient).getProductDetail(productId);
    verify(productMapper).productDetailDtoToProductDetail(dto);
  }

  @Test
  void getProductDetail_shouldThrowProductNotFoundException_whenProductNotFound() {
    String productId = "999";

    when(productExistingApiClient.getProductDetail(productId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productRepositoryAdapter.getProductDetail(productId))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessageContaining(productId);

    verify(productExistingApiClient).getProductDetail(productId);
    verify(productMapper, never()).productDetailDtoToProductDetail(any());
  }

  @Test
  void getSimilarIds_shouldReturnListOfIds_whenSimilarProductsExist() {
    String productId = "1";
    List<String> expectedIds = Arrays.asList("2", "3", "4");

    when(productExistingApiClient.getSimilarProductIds(productId))
        .thenReturn(Optional.of(expectedIds));

    List<String> result = productRepositoryAdapter.getSimilarIds(productId);

    assertThat(result).isNotNull();
    assertThat(result).hasSize(3);
    assertThat(result).containsExactly("2", "3", "4");

    verify(productExistingApiClient).getSimilarProductIds(productId);
  }

  @Test
  void getSimilarIds_shouldThrowProductNotFoundException_whenNoSimilarProducts() {
    String productId = "999";

    when(productExistingApiClient.getSimilarProductIds(productId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> productRepositoryAdapter.getSimilarIds(productId))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessageContaining(productId);

    verify(productExistingApiClient).getSimilarProductIds(productId);
  }

  @Test
  void getSimilarIds_shouldReturnEmptyList_whenApiReturnsEmptyList() {
    String productId = "1";
    List<String> emptyList = Arrays.asList();

    when(productExistingApiClient.getSimilarProductIds(productId))
        .thenReturn(Optional.of(emptyList));

    List<String> result = productRepositoryAdapter.getSimilarIds(productId);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();

    verify(productExistingApiClient).getSimilarProductIds(productId);
  }
}
