package com.rubenrbr.products.domain.port.out;

import java.util.List;

import com.rubenrbr.products.domain.model.ProductDetail;

public interface ProductRepository {

  ProductDetail getProductDetail(String productId);

  List<String> getSimilarIds(String productId);
}
