package com.github.pricemonitor.api;

import com.github.pricemonitor.model.request.alert.CreatePriceAlertRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "Price Alert")
@RequestMapping("api/v1/alert")
public interface PriceAlertApi {

    @Operation(summary = "Create price alert", description = "Creates a new price alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Price alert created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
    })
    @PostMapping
    ResponseEntity<Void> createAlert(
            @RequestBody @Valid final CreatePriceAlertRequest request,
            @AuthenticationPrincipal final UUID userPublicId);

}
