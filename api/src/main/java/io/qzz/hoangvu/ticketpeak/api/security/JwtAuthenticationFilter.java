package io.qzz.hoangvu.ticketpeak.api.security;

import io.jsonwebtoken.JwtException;
import io.qzz.hoangvu.ticketpeak.api.iam.model.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveBearerToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                JwtService.ParsedAccessToken parsedToken = jwtService.parseAccessToken(token);
                Set<Role> roles = parsedToken.roles();
                AuthenticatedAccount principal = new AuthenticatedAccount(parsedToken.accountId(), roles);
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
                        .toList();
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        token,
                        authorities
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveBearerToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }

        String token = header.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
