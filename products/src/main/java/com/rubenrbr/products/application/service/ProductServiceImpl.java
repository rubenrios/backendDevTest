package com.rubenrbr.products.application.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.domain.port.in.ProductService;
import com.rubenrbr.products.domain.port.out.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;

  @Override
  public Set<ProductDetail> getSimilarProducts(String productId) {
    return Set.of();
  }
}
