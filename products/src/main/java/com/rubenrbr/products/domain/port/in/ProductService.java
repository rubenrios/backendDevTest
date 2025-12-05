package com.rubenrbr.products.domain.port.in;

import java.util.Set;

import com.rubenrbr.products.domain.model.ProductDetail;

import reactor.core.publisher.Mono;

public interface ProductService {

  Mono<Set<ProductDetail>> getSimilarProducts(String productId);
}
