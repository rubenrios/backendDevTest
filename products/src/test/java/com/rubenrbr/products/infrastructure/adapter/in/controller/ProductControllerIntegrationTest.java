package com.rubenrbr.products.infrastructure.adapter.in.controller;

import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetail;
import static com.rubenrbr.products.infrastructure.util.TestUtil.createProductDetailDto;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.rubenrbr.products.domain.model.ProductDetail;
import com.rubenrbr.products.domain.port.in.ProductService;
import com.rubenrbr.products.infrastructure.rest.dto.ProductDetailDto;
import com.rubenrbr.products.infrastructure.rest.mapper.ProductResponseMapper;

@WebMvcTest(ProductController.class)
class ProductControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private ProductService productService;

  @MockBean private ProductResponseMapper mapper;

  @Test
  void getProductSimilar_shouldReturn200_whenProductsExist() throws Exception {
    String productId = "1";

    ProductDetail product1 = createProductDetail("2", "Product2");
    ProductDetail product2 = createProductDetail("3", "Product3");
    Set<ProductDetail> products = Set.of(product1, product2);

    ProductDetailDto dto1 = createProductDetailDto("2", "Product2");
    ProductDetailDto dto2 = createProductDetailDto("3", "Product3");
    Set<ProductDetailDto> productDtos = Set.of(dto1, dto2);

    when(productService.getSimilarProducts(productId)).thenReturn(products);
    when(mapper.productDetailToProductDetailDto(products)).thenReturn(productDtos);

    mockMvc
        .perform(
            get("/product/{productId}/similar", productId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(2));

    verify(productService).getSimilarProducts(productId);
    verify(mapper).productDetailToProductDetailDto(products);
  }

  @Test
  void getProductSimilar_shouldReturn200WithEmptyArray_whenNoSimilarProducts() throws Exception {
    String productId = "1";
    Set<ProductDetail> emptyProducts = Collections.emptySet();
    Set<ProductDetailDto> emptyDtos = Collections.emptySet();

    when(productService.getSimilarProducts(productId)).thenReturn(emptyProducts);
    when(mapper.productDetailToProductDetailDto(emptyProducts)).thenReturn(emptyDtos);

    mockMvc
        .perform(
            get("/product/{productId}/similar", productId).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(0))
        .andExpect(content().json("[]"));

    verify(productService).getSimilarProducts(productId);
    verify(mapper).productDetailToProductDetailDto(emptyProducts);
  }
}
