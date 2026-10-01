package com.cropdeal.order.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;

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
					        .requestMatchers("/admin/orders", "/admin/orders/**").hasRole("ADMIN")
					        .requestMatchers("/internal/admin/stats").hasRole("ADMIN")
					        .requestMatchers("/orders/**").authenticated()
					        .anyRequest().authenticated()
					    )
					.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
						(request, response, exception) -> response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or missing JWT")))
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).build();
	}
}