package com.github.pricemonitor.websocket;

import com.github.pricemonitor.properties.AppProperties;
import com.github.pricemonitor.security.TokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_PUBLIC_ID_ATTRIBUTE = "userPublicId";

    private final TokenProvider tokenProvider;
    private final AppProperties appProperties;

    @Override
    public boolean beforeHandshake(final ServerHttpRequest request,
                                   final ServerHttpResponse response,
                                   final WebSocketHandler wsHandler,
                                   final Map<String, Object> attributes) {
        if (!(request instanceof final ServletServerHttpRequest servletRequest)) {
            return false;
        }

        final String token = this.extractTokenFromCookies(servletRequest.getServletRequest());

        if (token == null) {
            log.debug("Rejected WebSocket handshake: no access token cookie present");
            return false;
        }

        try {
            final UUID userPublicId = this.tokenProvider.extractUserPublicId(token);
            attributes.put(USER_PUBLIC_ID_ATTRIBUTE, userPublicId);
            return true;
        } catch (final Exception _) {
            log.debug("Rejected WebSocket handshake: invalid access token");
            return false;
        }


    }

    @Override
    public void afterHandshake(final ServerHttpRequest request,
                               final ServerHttpResponse response,
                               final WebSocketHandler wsHandler,
                               @Nullable final Exception exception) {}

    @Nullable
    private String extractTokenFromCookies(final HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> this.appProperties.cookie().accessToken().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

}
