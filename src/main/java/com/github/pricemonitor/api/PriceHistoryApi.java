package com.github.pricemonitor.api;

import com.github.pricemonitor.model.dto.PriceHistory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Price History")
@RequestMapping("api/v1/history")
public interface PriceHistoryApi {

    @Operation(summary = "Get price history", description = "Fetches whole price history of product")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Price history retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PriceHistory.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid product url",
                    content = @Content(schema = @Schema())
            )
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<PriceHistory>> getPriceHistory(@RequestParam("productUrl") final String productUrl);

}