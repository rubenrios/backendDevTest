package com.rubenrbr.products.infrastructure.adapter.in.controller;

import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.domain.port.in.ProductService;
import com.rubenrbr.products.infrastructure.rest.ProductApi;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;
import com.rubenrbr.products.infrastructure.rest.mapper.ProductResponseMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProductController implements ProductApi {

  private final ProductService productService;
  private final ProductResponseMapper mapper;

  public ResponseEntity<Set<ProductDetailDto>> getProductSimilar(String productId) {
    Set<ProductDetail> products = productService.getSimilarProducts(productId);
    return ResponseEntity.ok(mapper.productDetailToProductDetailDto(products));
  }
}
