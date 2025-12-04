package com.rubenrbr.products.domain.port.out;

import java.util.List;

import com.rubenrbr.products.domain.model.ProductDetail;

import reactor.core.publisher.Mono;

public interface ProductRepository {

  Mono<ProductDetail> getProductDetail(String productId);

  Mono<List<String>> getSimilarIds(String productId);
}
