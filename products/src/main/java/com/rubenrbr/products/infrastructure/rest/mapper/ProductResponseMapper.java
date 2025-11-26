package com.rubenrbr.products.infrastructure.rest.mapper;

import java.util.Set;

import org.mapstruct.Mapper;

import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;

@Mapper(componentModel = "spring")
public interface ProductResponseMapper {

  ProductDetailDto productToProductDetail(ProductDetail product);

  Set<ProductDetailDto> productToProductDetail(Set<ProductDetail> product);
}
