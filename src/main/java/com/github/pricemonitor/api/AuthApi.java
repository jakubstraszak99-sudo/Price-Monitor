package com.github.pricemonitor.api;

import com.github.pricemonitor.request.UserRegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication")
@RequestMapping("/api/v1/auth")
public interface AuthApi {

    @Operation(description = "Creates a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already in use")
    })
    @PostMapping("/register")
    ResponseEntity<Void> register(@RequestBody final UserRegisterRequest request);

    @Operation(description = "Verifies the user account using the JWT token sent via email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @GetMapping("/verify")
    ResponseEntity<Void> verify(
            @Parameter(description = "JWT verification token", required = true)
            @RequestParam("token") final String token);
}
