package com.rubenrbr.products.domain.port.in;

import java.util.Set;

import com.rubenrbr.products.domain.model.ProductDetail;

public interface ProductService {

  Set<ProductDetail> getSimilarProducts(String productId);
}
