package com.pbl.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtTokenHelper {


    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expire-ms}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expire-ms}")
    private long refreshTokenExpiration;

    private Key secretKey;

    private final Set<String> invalidatedTokens = new HashSet<>();


    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }


    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public Long getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("userId", Long.class));
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> resolver) {
        final Claims claims = getAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims getAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public String generateAccessToken(UserDetails userDetails, Long userId) {
        return generateToken(userDetails, userId, accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails, Long userId) {
        return generateToken(userDetails, userId, refreshTokenExpiration);
    }

    private String generateToken(UserDetails userDetails, Long userId, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }


    public boolean validateToken(String token, UserDetails userDetails) {

        try {
            String username = getUsernameFromToken(token);

            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token)
                    && !invalidatedTokens.contains(token);

        } catch (ExpiredJwtException e) {
            System.out.println("Token expired");
        } catch (UnsupportedJwtException e) {
            System.out.println("Token unsupported");
        } catch (MalformedJwtException e) {
            System.out.println("Token malformed");
        } catch (SignatureException e) {
            System.out.println("Invalid token signature");
        } catch (IllegalArgumentException e) {
            System.out.println("Token claims string is empty");
        }

        return false;
    }

    public boolean isTokenExpired(String token) {
        return getExpirationFromToken(token).before(new Date());
    }



    public void invalidateToken(String token) {
        invalidatedTokens.add(token);
    }

    public boolean isInvalidated(String token) {
        return invalidatedTokens.contains(token);
    }


    public String rotateRefreshToken(String oldRefreshToken, UserDetails userDetails, Long userId) {
        invalidateToken(oldRefreshToken);
        return generateRefreshToken(userDetails, userId);
    }
}
