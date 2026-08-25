package com.github.pricemonitor.api;

import com.github.pricemonitor.model.request.password.ResetPasswordRequest;
import com.github.pricemonitor.model.request.password.UpdatePasswordRequest;
import com.github.pricemonitor.model.request.password.ForgotPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Tag(name = "User")
@RequestMapping("/api/v1/user")
public interface UserApi {

    @Operation(summary = "Password change", description = "Changes user password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password successfully updated"),
            @ApiResponse(responseCode = "400", description = "Old password does not match"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/password")
    ResponseEntity<Void> updatePassword(
            @RequestBody @Valid final UpdatePasswordRequest request,
            @AuthenticationPrincipal final UUID userPublicId);

    @Operation(summary = "Forgot password", description = "Sends a message with a password reset link")
    @ApiResponse(responseCode = "202", description = "Request received")
    @PostMapping("/password/forgot")
    ResponseEntity<Void> forgotPassword(@RequestBody @Valid final ForgotPasswordRequest request);

    @Operation(summary = "Password reset", description = "Resets forgotten password")
    @ApiResponse(responseCode = "200", description = "Password has been reset")
    @PatchMapping("/password/reset")
    ResponseEntity<Void> resetPassword(@RequestBody @Valid final ResetPasswordRequest request);

}
