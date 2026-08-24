package com.github.pricemonitor.api;

import com.github.pricemonitor.model.dto.ScrapedProduct;
import com.github.pricemonitor.request.ScrapeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Scraper")
@RequestMapping("/api/v1/scraper")
public interface ScraperApi {

    @Operation(description = "Extracts product info")
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
    @PostMapping
    ResponseEntity<ScrapedProduct> extractProductInfo(@RequestBody @Valid final ScrapeRequest request);

}
