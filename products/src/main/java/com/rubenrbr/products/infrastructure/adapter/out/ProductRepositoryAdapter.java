package com.rubenrbr.products.infrastructure.adapter.out;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rubenrbr.products.domain.exception.ProductNotFoundException;
import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.domain.port.out.ProductRepository;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

  private final ProductExistingApiClient productExistingApiClient;
  private final ProductMapper productMapper;

  @Override
  public ProductDetail getProductDetail(String productId) {
    ProductDetailDto dto =
        productExistingApiClient
            .getProductDetail(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));

    return productMapper.productDetailDtoToProductDetail(dto);
  }

  @Override
  public List<String> getSimilarIds(String productId) {
    return productExistingApiClient
        .getSimilarProductIds(productId)
        .orElseThrow(() -> new ProductNotFoundException(productId));
  }
}
