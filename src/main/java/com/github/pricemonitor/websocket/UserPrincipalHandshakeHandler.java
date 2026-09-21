package com.github.pricemonitor.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

public class UserPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(final ServerHttpRequest request,
                                      final WebSocketHandler wsHandler,
                                      final Map<String, Object> attributes) {
        final UUID userPublicId = (UUID) attributes.get(JwtHandshakeInterceptor.USER_PUBLIC_ID_ATTRIBUTE);
        final String name = userPublicId.toString();
        return () -> name;
    }

}
