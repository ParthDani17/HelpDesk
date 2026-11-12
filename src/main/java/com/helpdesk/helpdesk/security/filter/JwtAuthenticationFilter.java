package com.helpdesk.helpdesk.security.filter;

import com.helpdesk.helpdesk.security.service.JwtService;
import com.helpdesk.helpdesk.security.user.HelpDeskUserDetails;
import com.helpdesk.helpdesk.user.entity.User;
import com.helpdesk.helpdesk.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.helpdesk.helpdesk.refreshToken.service.RefreshTokenService;
import io.jsonwebtoken.JwtException;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository,
            RefreshTokenService refreshTokenService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {
            if (!jwtService.isTokenValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            UUID userId = UUID.fromString(jwtService.extractUserId(token));
            UUID sessionId = jwtService.extractSessionId(token);

            User user = userRepository.findById(userId).orElse(null);

            if (user == null || !user.isActive()) {
                filterChain.doFilter(request, response);
                return;
            }

            boolean sessionActive = refreshTokenService.isSessionActive(sessionId, userId);

            if (!sessionActive) {
                filterChain.doFilter(request, response);
                return;
            }

            HelpDeskUserDetails userDetails = new HelpDeskUserDetails(user);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}