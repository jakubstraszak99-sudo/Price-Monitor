package com.github.pricemonitor.api;

import com.github.pricemonitor.model.request.user.UserLoginRequest;
import com.github.pricemonitor.model.request.user.UserRegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.github.pricemonitor.utils.AuthenticationUtil.REFRESH_TOKEN_COOKIE;

@Tag(name = "Authentication")
@RequestMapping("/api/v1/auth")
public interface AuthApi {

    @Operation(summary = "User registration", description = "Creates a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already in use")
    })
    @PostMapping("/register")
    ResponseEntity<Void> register(@RequestBody @Valid final UserRegisterRequest request);

    @Operation(summary = "User verification", description = "Verifies the user account using the JWT token sent via email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @GetMapping("/verify")
    ResponseEntity<Void> verify(
            @Parameter(description = "JWT verification token", required = true)
            @RequestParam("token") final String token);

    @Operation(summary = "Login user", description = "Authenticates user and returns JWT tokens in HttpOnly cookies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Login successful",
                    headers = {
                            @Header(name = "Set-Cookie", description = "accessToken and refreshToken cookies")
                    }
            ),
            @ApiResponse(responseCode = "401", description = "Invalid credentials or unverified account")
    })
    @PostMapping("/login")
    ResponseEntity<Void> login(@RequestBody @Valid final UserLoginRequest request);

    @Operation(summary = "Refresh access token", description = "Generate a new access token")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    headers = {
                            @Header(name = "Set-Cookie", description = "New accessToken cookie")
                    }
            ),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    ResponseEntity<Void> refreshToken(@Parameter(hidden = true) @CookieValue(name = REFRESH_TOKEN_COOKIE) final String refreshToken);
}
