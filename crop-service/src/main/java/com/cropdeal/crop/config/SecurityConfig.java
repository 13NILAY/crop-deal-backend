package com.cropdeal.crop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                		.requestMatchers(
					            "/swagger-ui/**",
					            "/swagger-ui.html",
					            "/v3/api-docs/**"
					        ).permitAll()
                            .requestMatchers("/admin/crops", "/admin/crops/**").hasRole("ADMIN")
                        .requestMatchers("/internal/admin/stats").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.POST, "/crops/*/reserve").hasRole("DEALER")
                            .requestMatchers(HttpMethod.GET, "/crops/mine").hasRole("FARMER")
                        .requestMatchers(HttpMethod.GET, "/crops", "/crops/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/crops").hasRole("FARMER")
                        .requestMatchers(HttpMethod.PUT, "/crops/**").hasRole("FARMER")
                        .requestMatchers(HttpMethod.DELETE, "/crops/**").hasRole("FARMER")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}