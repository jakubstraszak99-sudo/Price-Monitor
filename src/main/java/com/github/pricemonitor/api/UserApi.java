package com.github.pricemonitor.api;

import com.github.pricemonitor.model.dto.User;
import com.github.pricemonitor.model.request.password.ForgotPasswordRequest;
import com.github.pricemonitor.model.request.password.ResetPasswordRequest;
import com.github.pricemonitor.model.request.password.UpdatePasswordRequest;
import com.github.pricemonitor.model.request.user.UpdateUserSettingsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User")
@RequestMapping("/api/v1/user")
public interface UserApi {

    @Operation(summary = "Get user", description = "Fetches user data")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User data retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema())
            )
    })
    @GetMapping
    ResponseEntity<User> getUser(@AuthenticationPrincipal final UUID userPublicId);

    @Operation(summary = "Update user settings", description = "Enables or disables price alert emails for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Settings updated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid settings",
                    content = @Content(schema = @Schema())),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(schema = @Schema()))
    })
    @PatchMapping("/settings")
    ResponseEntity<User> updateSettings(
            @RequestBody @Valid final UpdateUserSettingsRequest request,
            @AuthenticationPrincipal final UUID userPublicId);

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
