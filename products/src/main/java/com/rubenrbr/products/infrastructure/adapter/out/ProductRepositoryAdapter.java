package com.rubenrbr.products.infrastructure.adapter.out;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rubenrbr.products.domain.exception.ProductNotFoundException;
import com.rubenrbr.products.domain.port.out.ProductRepository;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

  private final ProductExistingApiClient productExistingApiClient;

  @Override
  public ProductDetailDto getProductDetail(String productId) {
    return productExistingApiClient
        .getProductDetail(productId)
        .orElseThrow(() -> new ProductNotFoundException(productId));
  }

  @Override
  public List<String> getSimilarIds(String productId) {
    return productExistingApiClient
        .getSimilarProductIds(productId)
        .orElseThrow(() -> new ProductNotFoundException(productId));
  }
}
