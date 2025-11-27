package com.rubenrbr.products.infrastructure.adapter.out;

import org.mapstruct.Mapper;

import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;

@Mapper(componentModel = "spring")
public interface ProductMapper {

  ProductDetail productDetailDtoToProductDetail(ProductDetailDto product);
}
