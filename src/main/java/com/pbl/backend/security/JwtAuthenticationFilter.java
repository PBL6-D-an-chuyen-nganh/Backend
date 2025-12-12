package com.pbl.backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private CustomUserDetailService customUserDetailService;

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestToken = request.getHeader("Authorization");
        String username = null;
        String token = null;

        if (requestToken != null && requestToken.startsWith("Bearer ")) {
            token = requestToken.substring(7);
            try {
                username = this.jwtTokenHelper.getUsernameFromToken(token);
            } catch (IllegalArgumentException e) {
                logger.error("Unable to get Jwt token");
            } catch (ExpiredJwtException e) {
                logger.warn("Jwt token has expired");
            } catch (MalformedJwtException e) {
                logger.error("Invalid jwt");
            } catch (Exception e) {
                logger.error("Jwt validation error: {}", e.getMessage());
            }
        } else {
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.customUserDetailService.loadUserByUsername(username);

            if (this.jwtTokenHelper.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("DEBUG: User " + username + " authenticated successfully with authorities: " + userDetails.getAuthorities());
            }
            else {
                    logger.error("DEBUG: Token validation failed for user: " + username);
            }
        }
        filterChain.doFilter(request, response);
    }
}