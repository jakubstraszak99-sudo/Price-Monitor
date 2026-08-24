package com.github.pricemonitor.request;

import jakarta.validation.constraints.NotBlank;

public record ScrapeRequest(

        @NotBlank
        String url
) {}
