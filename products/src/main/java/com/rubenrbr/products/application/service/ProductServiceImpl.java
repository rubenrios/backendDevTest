package com.rubenrbr.products.application.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.rubenrbr.products.domain.exception.ProductNotFoundException;
import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.domain.port.in.ProductService;
import com.rubenrbr.products.domain.port.out.ProductRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;

  @Override
  public Mono<Set<ProductDetail>> getSimilarProducts(String productId) {
    return productRepository
        .getSimilarIds(productId)
        .flatMapMany(Flux::fromIterable)
        .flatMap(
            id ->
                productRepository
                    .getProductDetail(id)
                    .onErrorResume(ProductNotFoundException.class, e -> Mono.empty()))
        .collect(Collectors.toSet());
  }
}
