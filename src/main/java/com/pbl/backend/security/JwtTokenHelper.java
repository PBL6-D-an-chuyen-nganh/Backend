package com.pbl.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtTokenHelper {

    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;

    private Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private Set<String> invalidatedTokens = new HashSet<>();

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(UserDetails userDetails, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername(), userId);
    }
    public Integer getUserIdFromToken(String token) {
        return getClaimFromToken(token, claims -> Integer.parseInt(claims.get("userId").toString()));
    }

    private String doGenerateToken(Map<String, Object> claims, String subject, Integer userId) {
        claims.put("userId", userId); // Thêm userId vào claims
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(secretKey)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final Integer userId = getUserIdFromToken(token);
        return (userId.equals(userDetails.getUsername()) && !isTokenExpired(token) && !invalidatedTokens.contains(token));
    }

    public void invalidateToken(String token) {
        invalidatedTokens.add(token);
    }
}