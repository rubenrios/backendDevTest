package com.rubenrbr.products.infrastructure.util;

import java.math.BigDecimal;

import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;

public class TestUtil {

  public static ProductDetail createProductDetail(
      String id, String name, BigDecimal price, boolean availability) {
    return ProductDetail.builder()
        .id(id)
        .name(name)
        .price(price)
        .availability(availability)
        .build();
  }

  public static ProductDetailDto createProductDetailDto(
      String id, String name, BigDecimal price, boolean availability) {
    return new ProductDetailDto(id, name, price, availability);
  }
}
