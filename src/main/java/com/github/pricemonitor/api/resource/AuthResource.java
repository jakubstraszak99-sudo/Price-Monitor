package com.github.pricemonitor.api.resource;

import com.github.pricemonitor.api.AuthApi;
import com.github.pricemonitor.request.UserRegisterRequest;
import com.github.pricemonitor.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthResource implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<Void> register(final UserRegisterRequest request) {
        this.authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> verify(final String token) {
        this.authService.verifyAccount(token);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
