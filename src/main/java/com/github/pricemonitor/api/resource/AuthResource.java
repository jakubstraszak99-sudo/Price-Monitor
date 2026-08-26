package com.github.pricemonitor.api.resource;

import com.github.pricemonitor.api.AuthApi;
import com.github.pricemonitor.model.dto.AccessTokenExpiryInfo;
import com.github.pricemonitor.model.dto.AuthTokenSet;
import com.github.pricemonitor.model.request.user.UserLoginRequest;
import com.github.pricemonitor.model.request.user.UserRegisterRequest;
import com.github.pricemonitor.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static com.github.pricemonitor.utils.AuthenticationUtil.*;

@RestController
@RequiredArgsConstructor
public class AuthResource implements AuthApi {

    private final AuthService authService;

    @Override
    public ResponseEntity<Void> register(final UserRegisterRequest request) {
        this.authService.registerUser(request.username(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> verify(final String token) {
        this.authService.verifyAccount(token);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Override
    public ResponseEntity<Void> login(final UserLoginRequest request) {
        final AuthTokenSet authTokenSet = this.authService.login(request.login(), request.password());
        final ResponseCookie accessCookie = this.buildAccessCookie(authTokenSet.accessToken(), authTokenSet.accessExpirationSeconds());
        final ResponseCookie refreshCookie = this.buildRefreshCookie(authTokenSet.refreshToken(), authTokenSet.refreshExpirationSeconds());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @Override
    public ResponseEntity<Void> logout(final String refreshToken) {
        this.authService.logout(refreshToken);

        final ResponseCookie clearAccessCookie = this.buildCookie(ACCESS_TOKEN_COOKIE, "", ACCESS_TOKEN_PATH, 0);
        final ResponseCookie clearRefreshCookie = this.buildCookie(REFRESH_TOKEN_COOKIE, "", REFRESH_TOKEN_PATH, 0);

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearAccessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, clearRefreshCookie.toString())
                .build();
    }

    @Override
    public ResponseEntity<Void> refreshToken(final String refreshToken) {
        final AccessTokenExpiryInfo accessTokenExpiryInfo = this.authService.refreshToken(refreshToken);
        final ResponseCookie accessCookie = this.buildAccessCookie(accessTokenExpiryInfo.accessToken(), accessTokenExpiryInfo.accessExpirationSeconds());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .build();
    }

    private ResponseCookie buildAccessCookie(final String value, final long maxAgeSeconds) {
        return this.buildCookie(ACCESS_TOKEN_COOKIE, value, ACCESS_TOKEN_PATH, maxAgeSeconds);
    }

    private ResponseCookie buildRefreshCookie(final String value, final long maxAgeSeconds) {
        return this.buildCookie(REFRESH_TOKEN_COOKIE, value, REFRESH_TOKEN_PATH, maxAgeSeconds);
    }

    private ResponseCookie buildCookie(final String name, final String value, final String path, final long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .path(path)
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")
                .build();
    }

}
