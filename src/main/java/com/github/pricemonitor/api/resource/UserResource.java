package com.github.pricemonitor.api.resource;

import com.github.pricemonitor.api.UserApi;
import com.github.pricemonitor.model.request.password.ResetPasswordRequest;
import com.github.pricemonitor.model.request.password.UpdatePasswordRequest;
import com.github.pricemonitor.model.request.password.ForgotPasswordRequest;
import com.github.pricemonitor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserResource implements UserApi {

    private final UserService userService;

    @Override
    public ResponseEntity<Void> updatePassword(final UpdatePasswordRequest request, final UUID userPublicId) {
        this.userService.updatePassword(userPublicId, request.oldPassword(), request.newPassword());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<Void> forgotPassword(final ForgotPasswordRequest request) {
        this.userService.forgotPassword(request.email());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    public ResponseEntity<Void> resetPassword(final ResetPasswordRequest request) {
        this.userService.resetPassword(request.resetToken(), request.newPassword());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
