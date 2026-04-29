package org.unamur.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;


// Not used anymore
@Deprecated
public class ApiKeyFilter extends OncePerRequestFilter {
    private final String requiredToken;

    public ApiKeyFilter(String requiredToken) {
        this.requiredToken = requiredToken;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.equals("Bearer " + requiredToken)) {
            // Manually set authentication in context
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    "GitHubAction", null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}