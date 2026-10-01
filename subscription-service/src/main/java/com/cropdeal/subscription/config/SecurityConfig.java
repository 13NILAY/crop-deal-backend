package com.cropdeal.subscription.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter filter) throws Exception {
		return http.csrf(csrf -> csrf.disable())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
					            "/swagger-ui/**",
					            "/swagger-ui.html",
								"/actuator/health",
							"/v3/api-docs",
					            "/v3/api-docs/**"
					        ).permitAll()
						// .requestMatchers("/actuator/health", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/subscriptions/farmers/*/dealers").permitAll()
						.requestMatchers("/internal/admin/stats").hasRole("ADMIN")
						.requestMatchers("/admin/subscriptions", "/admin/subscriptions/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/subscriptions/farmers/**").hasRole("DEALER")
						.requestMatchers(HttpMethod.DELETE, "/subscriptions/farmers/**").hasRole("DEALER")
						.requestMatchers("/subscriptions/**").hasRole("DEALER").anyRequest().authenticated())
				.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
						new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
	}
}
