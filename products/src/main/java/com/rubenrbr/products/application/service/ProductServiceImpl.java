package com.rubenrbr.products.application.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.rubenrbr.products.domain.exception.ProductNotFoundException;
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
    List<String> ids = productRepository.getSimilarIds(productId);

    return ids.stream()
        .map(
            id -> {
              try {
                return productRepository.getProductDetail(id);
              } catch (ProductNotFoundException e) {
                return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }
}
