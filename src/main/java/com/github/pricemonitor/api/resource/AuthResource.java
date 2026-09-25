package com.github.pricemonitor.api.resource;

import com.github.pricemonitor.api.AuthApi;
import com.github.pricemonitor.model.dto.AccessTokenExpiryData;
import com.github.pricemonitor.model.dto.AuthTokenSet;
import com.github.pricemonitor.model.request.user.UserLoginRequest;
import com.github.pricemonitor.model.request.user.UserRegisterRequest;
import com.github.pricemonitor.properties.AppProperties;
import com.github.pricemonitor.service.AuthService;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.CacheControl;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AuthResource implements AuthApi {

    private final AuthService authService;
    private final AppProperties appProperties;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final CookieCsrfTokenRepository csrfTokenRepository;

    @Override
    public ResponseEntity<Void> csrf() {
        return ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build();
    }

    private void rotateCsrfToken() {
        this.csrfTokenRepository.saveToken(this.csrfTokenRepository.generateToken(this.request), this.request, this.response);
    }

    @Override
    public ResponseEntity<Void> register(final UserRegisterRequest request) {
        this.authService.registerUser(request.username(), request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> verify(final String token) {
        final AuthTokenSet authTokenSet = this.authService.verifyAccount(token);
        this.rotateCsrfToken();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, this.buildAccessCookie(
                        authTokenSet.accessToken(),
                        authTokenSet.accessExpirationSeconds())
                        .toString())
                .header(HttpHeaders.SET_COOKIE, this.buildRefreshCookie(
                        authTokenSet.refreshToken(),
                        authTokenSet.refreshExpirationSeconds())
                        .toString())
                .build();
    }

    @Override
    public ResponseEntity<Void> login(final UserLoginRequest request) {
        final AuthTokenSet authTokenSet = this.authService.login(request.login(), request.password());
        this.rotateCsrfToken();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, this.buildAccessCookie(
                        authTokenSet.accessToken(),
                        authTokenSet.accessExpirationSeconds())
                        .toString())
                .header(HttpHeaders.SET_COOKIE, this.buildRefreshCookie(
                        authTokenSet.refreshToken(),
                        authTokenSet.refreshExpirationSeconds())
                        .toString())
                .build();
    }

    @Override
    public ResponseEntity<Void> logout() {
        this.authService.logout(this.getRefreshToken());
        this.rotateCsrfToken();
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.SET_COOKIE, this.buildAccessCookie("", 0).toString())
                .header(HttpHeaders.SET_COOKIE, this.buildRefreshCookie("", 0).toString())
                .build();
    }

    @Override
    public ResponseEntity<Void> refreshToken() {
        final AccessTokenExpiryData data = this.authService.refreshToken(this.getRefreshToken());
        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, this.buildAccessCookie(
                        data.accessToken(),
                        data.accessExpirationSeconds())
                        .toString())
                .build();
    }

    @Nullable
    private String getRefreshToken() {
        return Optional.ofNullable(this.request.getCookies())
                .stream()
                .flatMap(Arrays::stream)
                .filter(cookie -> this.appProperties.cookie().refreshToken().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private ResponseCookie buildAccessCookie(final String value, final long maxAgeSeconds) {
        return this.buildCookie(this.appProperties.cookie().accessToken(), value, this.appProperties.paths().accessToken(), maxAgeSeconds);
    }

    private ResponseCookie buildRefreshCookie(final String value, final long maxAgeSeconds) {
        return this.buildCookie(this.appProperties.cookie().refreshToken(), value, this.appProperties.paths().refreshToken(), maxAgeSeconds);
    }

    private ResponseCookie buildCookie(final String name, final String value, final String path, final long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(this.appProperties.cookie().secure())
                .path(path)
                .maxAge(maxAgeSeconds)
                .sameSite("Strict")
                .build();
    }

}