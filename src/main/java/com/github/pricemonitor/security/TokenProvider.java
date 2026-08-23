package com.github.pricemonitor.security;

import com.github.pricemonitor.exception.PmRuntimeException;
import com.github.pricemonitor.redis.RefreshToken;
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
import java.util.UUID;

import static com.github.pricemonitor.exception.ExceptionCode.E003;
import static com.github.pricemonitor.exception.ExceptionCode.E004;

@Component
public class TokenProvider {

    private final SecretKey key;
    private final long verificationExpirationMs;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public TokenProvider(
            @Value("${app.jwt.secret}") final String secret,
            @Value("${app.jwt.verification-expiration-ms}") final long verificationExpirationMs,
            @Value("${app.jwt.access-expiration-ms}") final long accessExpirationMs,
            @Value("${app.jwt.refresh-expiration-ms}") final long refreshExpirationMs) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.verificationExpirationMs = verificationExpirationMs;
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateVerificationToken(final UUID userPublicId) {
        return this.buildToken(userPublicId, this.verificationExpirationMs);
    }

    public String generateAccessToken(final UUID userPublicId) {
        return this.buildToken(userPublicId, this.accessExpirationMs);
    }

    public RefreshToken generateRefreshToken(final UUID userPublicId) {
        return RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .userPublicId(userPublicId)
                .expirationInSeconds(this.refreshExpirationMs / 1000)
                .build();
    }

    public UUID extractUserPublicId(final String token) {
        try {
            final Claims claims = Jwts.parser()
                    .verifyWith(this.key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return UUID.fromString(claims.getSubject());
        } catch (final ExpiredJwtException e) {
            throw new PmRuntimeException(E003, e);
        } catch (final SignatureException e) {
            throw new PmRuntimeException(E004, e);
        }
    }

    private String buildToken(final UUID userPublicId, final long expirationTimeMs) {
        final Date currentDate = new Date();
        final Date expirationDate = new Date(currentDate.getTime() + expirationTimeMs);

        return Jwts.builder()
                .subject(userPublicId.toString())
                .issuedAt(currentDate)
                .expiration(expirationDate)
                .signWith(this.key, Jwts.SIG.HS256)
                .compact();
    }

}
