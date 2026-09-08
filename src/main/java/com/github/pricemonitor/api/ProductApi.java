package com.github.pricemonitor.api;

import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.model.page.ProductPage;
import com.github.pricemonitor.model.request.product.ProductInfoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Product")
@RequestMapping("/api/v1/product")
public interface ProductApi {

    @Operation(summary = "Product scraping", description = "Extracts product info")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product info successfully extracted",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ScrapedProduct.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid scrape request",
                    content = @Content(schema = @Schema())
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Unable to extract product info from the given source",
                    content = @Content(schema = @Schema())
            ),
            @ApiResponse(
                    responseCode = "504",
                    description = "Timed out while scraping the target source",
                    content = @Content(schema = @Schema())
            )
    })
    @PostMapping("/info/preview")
    ResponseEntity<ScrapedProduct> extractProductInfo(@RequestBody @Valid final ProductInfoRequest request);

    @Operation(summary = "Fetch products", description = "Returns a paginated list of products from database")
    @ApiResponse(
            responseCode = "200",
            description = "Page of products retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProductPage.class)
            )
    )
    @GetMapping
    ResponseEntity<ProductPage> getProducts(
            @ParameterObject @PageableDefault(size = 20, sort = "name") final Pageable pageable,
            @Parameter(description = "Filter products by name (case-insensitive, partial match)")
            @RequestParam(required = false) final String search
    );

}
