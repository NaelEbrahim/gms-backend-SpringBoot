package com.graduation.GMS.Filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graduation.GMS.Models.Enums.Roles;
import com.graduation.GMS.Repositories.AuthTokenRepository;
import com.graduation.GMS.Services.GeneralServices.JwtService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthTokenRepository authTokenRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final List<String> NO_AUTH_PATHS = List.of(
            "/api/user/login",
            "api/user/refresh",
            "/api/user/forgotPassword",
            "/api/user/verifyResetCode",
            "/api/user/resetForgotPassword"
    );

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain
    ) throws IOException {
        try {
            final String path = request.getServletPath();
            if (NO_AUTH_PATHS.contains(path)) {
                filterChain.doFilter(request, response);
                return;
            }

            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String token = authHeader.substring(7);

            if (!jwtService.validateToken(token) || authTokenRepository.findByAccessToken(token).isEmpty()) {
                throw new BadCredentialsException("Invalid Authorization header");
            }

            String userId = jwtService.extractId(token);
            List<Roles> roles = jwtService.extractRoles(token);

            List<GrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority(role.name()))
                    .collect(Collectors.toList());

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userId, token, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (BadCredentialsException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void sendErrorResponse(@NotNull HttpServletResponse response, int status, String message) throws IOException {
        SecurityContextHolder.clearContext();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status);
        objectMapper.writeValue(response.getWriter(), Map.of(
                "status", status,
                "message", message
        ));
    }
}
