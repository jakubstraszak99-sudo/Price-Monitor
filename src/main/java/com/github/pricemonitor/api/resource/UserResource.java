package com.github.pricemonitor.api.resource;

import com.github.pricemonitor.api.UserApi;
import com.github.pricemonitor.request.ChangePasswordRequest;
import com.github.pricemonitor.request.ResetPasswordRequest;
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
    public ResponseEntity<Void> resetPassword(final ResetPasswordRequest request) {
        this.userService.resetPassword(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    public ResponseEntity<Void> changePassword(final ChangePasswordRequest request, final UUID userPublicId) {
        this.userService.changePassword(userPublicId, request.password());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
