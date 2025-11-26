package com.rubenrbr.products.domain.port.out;

import java.util.List;

import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;

public interface ProductRepository {

  ProductDetailDto getProductDetail(String productId);

  List<String> getSimilarIds(String productId);
}
