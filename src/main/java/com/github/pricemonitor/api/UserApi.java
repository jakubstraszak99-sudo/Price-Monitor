package com.github.pricemonitor.api;

import com.github.pricemonitor.request.ResetPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "User")
@RequestMapping("/api/v1/user")
public interface UserApi {

    @Operation(description = "Sends a message with a password reset link")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Request received")
    })
    @PostMapping("/reset-password")
    ResponseEntity<Void> resetPassword(@RequestBody final ResetPasswordRequest request);

}
