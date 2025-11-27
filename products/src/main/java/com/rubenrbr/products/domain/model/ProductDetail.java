package com.rubenrbr.products.domain.model;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record ProductDetail(
    @NotBlank String id,
    @NotBlank String name,
    @NotNull @Positive BigDecimal price,
    @NotNull Boolean availability) {}
