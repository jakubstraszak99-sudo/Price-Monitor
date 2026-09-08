package com.github.pricemonitor.api;

import com.github.pricemonitor.model.page.PriceAlertPage;
import com.github.pricemonitor.model.request.alert.CreatePriceAlertRequest;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Price Alert")
@RequestMapping("api/v1/alert")
public interface PriceAlertApi {

    @Operation(summary = "Create price alert", description = "Creates a new price alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Price alert created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Price alert already exists")
    })
    @PostMapping
    ResponseEntity<Void> createAlert(
            @RequestBody @Valid final CreatePriceAlertRequest request,
            @AuthenticationPrincipal final UUID userPublicId);

    @Operation(summary = "Fetch price alerts", description = "Returns a paginated list of price alerts")
    @ApiResponse(
            responseCode = "200",
            description = "Page of products retrieved successfully",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PriceAlertPage.class)
            )
    )
    @GetMapping
    ResponseEntity<PriceAlertPage> getAlerts(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt") final Pageable pageable,
            @Parameter(description = "Filter price alerts by product name (case-insensitive, partial match)")
            @RequestParam(required = false) final String search,
            @AuthenticationPrincipal final UUID userPublicId
    );
}
