package com.cropdeal.payment.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers("/payments/webhook").permitAll()
                		.requestMatchers(
					            "/swagger-ui/**",
					            "/swagger-ui.html",
					            "/v3/api-docs/**"
					        ).permitAll()
                            .requestMatchers("/admin/payments", "/admin/payments/**").hasRole("ADMIN")
                            .requestMatchers("/internal/admin/stats").hasRole("ADMIN")
                        .requestMatchers("/payments/**", "/bank-accounts/**", "/payouts/farmer/me").authenticated()
                        .requestMatchers("/payouts/**").hasRole("ADMIN").anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).build();
    }
}