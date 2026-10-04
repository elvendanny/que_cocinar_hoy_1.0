package com.cocinacero.cocina.security;

import com.cocinacero.comun.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
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
  SecurityFilterChain chain(HttpSecurity http, AuthCliente auth, ObjectMapper json)
      throws Exception {
    var filter =
        new OncePerRequestFilter() {
          protected void doFilterInternal(
              HttpServletRequest req, HttpServletResponse res, FilterChain next)
              throws ServletException, IOException {
            try {
              if (!req.getRequestURI().equals("/health")) {
                var a = auth.verificar(req.getHeader("Authorization"));
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
        .authorizeHttpRequests(a -> a.anyRequest().permitAll())
        .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
