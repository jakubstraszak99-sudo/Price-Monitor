package com.github.pricemonitor.api;

import com.github.pricemonitor.request.ChangePasswordRequest;
import com.github.pricemonitor.request.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "User")
@RequestMapping("/api/v1/user")
public interface UserApi {

    @Operation(description = "Sends a message with a password reset link")
    @ApiResponse(responseCode = "202", description = "Request received")
    @PostMapping("/reset-password")
    ResponseEntity<Void> resetPassword(@RequestBody @Valid final ResetPasswordRequest request);

    @Operation(description = "Changes user password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password successfully changed"),
            @ApiResponse(responseCode = "400", description = "Password cannot be changed")
    })
    @PatchMapping("/change-password")
    ResponseEntity<Void> changePassword(@RequestBody @Valid final ChangePasswordRequest request);

}
