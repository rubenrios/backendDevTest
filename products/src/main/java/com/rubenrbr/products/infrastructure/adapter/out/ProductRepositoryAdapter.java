package com.rubenrbr.products.infrastructure.adapter.out;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.domain.port.out.ProductRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

  private final ProductExistingApiClient productExistingApiClient;
  private final ProductMapper productMapper;

  @Override
  public Mono<ProductDetail> getProductDetail(String productId) {
    return productExistingApiClient
        .getProductDetail(productId)
        .map(productMapper::productDetailDtoToProductDetail);
  }

  @Override
  public Mono<List<String>> getSimilarIds(String productId) {
    return productExistingApiClient.getSimilarProductIds(productId);
  }
}
