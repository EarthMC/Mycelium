package net.earthmc.mycelium.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Component
@NullMarked
public class AuthenticationFilter extends OncePerRequestFilter {
    private final byte[] backendToken;
    private final byte[] accessToken;

    public AuthenticationFilter(@Value("${security.backend-token}") String backendToken, @Value("${security.frontend-access-token}") String accessToken) {
        this.backendToken = backendToken.getBytes(StandardCharsets.UTF_8);
        this.accessToken = accessToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        final String authorization = request.getHeader("Authorization");
        final String accessToken = request.getHeader("X-Mycelium-Access");

        if (authorization == null || !authorization.startsWith("Bearer ") || accessToken == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        final String backendToken = authorization.substring("Bearer ".length());
        final boolean valid = MessageDigest.isEqual(this.backendToken, backendToken.getBytes(StandardCharsets.UTF_8)) && MessageDigest.isEqual(this.accessToken, accessToken.getBytes(StandardCharsets.UTF_8));

        if (!valid) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
            "backend",
            null,
            List.of(new SimpleGrantedAuthority("ROLE_BACKEND"))
        ));

        filterChain.doFilter(request, response);
    }
}
