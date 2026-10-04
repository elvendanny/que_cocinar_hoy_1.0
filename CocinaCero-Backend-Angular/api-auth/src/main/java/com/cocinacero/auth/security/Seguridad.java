package com.cocinacero.auth.security;

import com.cocinacero.auth.service.AuthService;
import com.cocinacero.comun.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class Seguridad {
  @Bean
  SecurityFilterChain chain(
      HttpSecurity http,
      AuthService auth,
      ObjectMapper json,
      @Value("${app.internal-key}") String internal)
      throws Exception {
    if (internal.length() < 32)
      throw new IllegalStateException("Configura app.internal-key con al menos 32 caracteres.");
    var limit = new LimiteIntentos();
    var filter =
        new OncePerRequestFilter() {
          protected void doFilterInternal(
              HttpServletRequest req, HttpServletResponse res, FilterChain next)
              throws ServletException, IOException {
            try {
              String p = req.getRequestURI();
              boolean pub =
                  p.equals("/health")
                      || Set.of(
                              "/api/auth/registro",
                              "/api/auth/verificar",
                              "/api/auth/reenviar",
                              "/api/auth/login")
                          .contains(p);
              if (pub) {
                if (!p.equals("/health")) limit.comprobar(req.getRemoteAddr());
              } else {
                if (p.startsWith("/api/internal/")
                    && !MessageDigest.isEqual(
                        internal.getBytes(StandardCharsets.UTF_8),
                        Objects.toString(req.getHeader("X-Internal-Key"), "")
                            .getBytes(StandardCharsets.UTF_8)))
                  throw new ApiError(401, "Credencial de servicio inválida.");
                String header = req.getHeader("Authorization");
                if (header == null || !header.startsWith("Bearer "))
                  throw new ApiError(401, "Se requiere Bearer Token.");
                var a = auth.autenticar(header.substring(7));
                SecurityContextHolder.getContext()
                    .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                            a, null, List.of(new SimpleGrantedAuthority("ROLE_" + a.rol()))));
              }
              next.doFilter(req, res);
            } catch (ApiError e) {
              res.setStatus(e.status);
              res.setContentType("application/json;charset=UTF-8");
              json.writeValue(res.getWriter(), Map.of("error", e.getMessage()));
            } finally {
              SecurityContextHolder.clearContext();
            }
          }
        };
    return http.cors(org.springframework.security.config.Customizer.withDefaults())
        .csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/api/usuarios/**", "/api/roles")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .permitAll())
        .exceptionHandling(
            e ->
                e.accessDeniedHandler(
                    (req, res, ex) -> {
                      res.setStatus(403);
                      res.setContentType("application/json");
                      json.writeValue(res.getWriter(), Map.of("error", "Se requiere rol ADMIN."));
                    }))
        .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
