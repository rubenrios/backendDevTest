package com.rubenrbr.products.infrastructure.adapter.in.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.rubenrbr.products.domain.port.in.ProductService;
import com.rubenrbr.products.infrastructure.rest.ProductApi;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;
import com.rubenrbr.products.infrastructure.rest.mapper.ProductResponseMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {

  private final ProductService productService;
  private final ProductResponseMapper mapper;

  @Override
  public Mono<ResponseEntity<Flux<ProductDetailDto>>> getProductSimilar(
      String productId, ServerWebExchange exchange) {
    return productService
        .getSimilarProducts(productId)
        .map(mapper::productDetailToProductDetailDto)
        .map(productList -> ResponseEntity.ok(Flux.fromIterable(productList)));
  }
}
