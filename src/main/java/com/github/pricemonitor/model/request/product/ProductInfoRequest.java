package com.github.pricemonitor.model.request.product;

import jakarta.validation.constraints.NotBlank;

public record ProductInfoRequest(

        @NotBlank
        String url

) {}
