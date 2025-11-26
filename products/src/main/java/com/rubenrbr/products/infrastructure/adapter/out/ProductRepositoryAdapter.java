package com.rubenrbr.products.infrastructure.adapter.out;

import org.springframework.stereotype.Component;

import com.rubenrbr.products.domain.port.out.ProductRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {}
