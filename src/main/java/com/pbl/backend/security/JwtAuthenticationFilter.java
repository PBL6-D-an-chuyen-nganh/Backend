package com.pbl.backend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @Autowired
    private CustomUserDetailService customUserDetailService;

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        // Bỏ qua xử lý token cho các route public
        if (path.startsWith("/api/v1/auth")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/v2/api-docs")
                || path.startsWith("/swagger")
                || path.startsWith("/webjars/")
                || path.equals("/api/payment/create")
                || path.equals("/api/payment/return")
                || path.equals("/payment-success")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestToken = request.getHeader("Authorization");

        // Nếu không có token hoặc token sai định dạng → bỏ qua
        if (requestToken == null || !requestToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = requestToken.substring(7);
        String username = null;

        try {
            username = this.jwtTokenHelper.getUsernameFromToken(token);
        } catch (IllegalArgumentException e) {
            System.out.println("Unable to get Jwt token");
        } catch (ExpiredJwtException e) {
            System.out.println("Jwt token has expired");
        } catch (MalformedJwtException e) {
            System.out.println("Invalid jwt");
        }

        // Nếu token hợp lệ → set authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.customUserDetailService.loadUserByUsername(username);

            if (this.jwtTokenHelper.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                System.out.println("Invalid jwt token");
            }
        }

        filterChain.doFilter(request, response);
    }

}
