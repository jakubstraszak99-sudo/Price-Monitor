package com.github.pricemonitor.security;

import com.github.pricemonitor.exception.PmRuntimeException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

import static com.github.pricemonitor.exception.ExceptionCode.E003;
import static com.github.pricemonitor.exception.ExceptionCode.E004;

@Component
public class TokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    public TokenProvider(
            @Value("${app.jwt.secret}") final String secret,
            @Value("${app.jwt.expiration-ms}") final long expirationMs) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateVerificationToken(final String email) {
        final Date currentDate = new Date();
        final Date expirationDate = new Date(currentDate.getTime() + this.expirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(currentDate)
                .expiration(expirationDate)
                .signWith(this.key, Jwts.SIG.HS256)
                .compact();
    }

    public String extractEmail(final String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(this.key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (final ExpiredJwtException e) {
            throw new PmRuntimeException(E003, e);
        } catch (final SignatureException e) {
            throw new PmRuntimeException(E004, e);
        }
    }

}
