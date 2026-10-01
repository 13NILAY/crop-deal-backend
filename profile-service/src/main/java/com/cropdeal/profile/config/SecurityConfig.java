package com.cropdeal.profile.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter filter) throws Exception {
        return http.csrf(csrf -> csrf.disable())
        		
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.requestMatchers(
                	    "/auth/**",
                	    "/swagger-ui/**",
                	    "/v3/api-docs/**",
                	    "/swagger-ui.html"
                	).permitAll()
                    .requestMatchers("/admin/verifications", "/admin/verifications/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/dealers/emails").permitAll()
                    .requestMatchers(HttpMethod.GET, "/farmers/**", "/dealers/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/farmers/**").hasRole("FARMER")
                    .requestMatchers(HttpMethod.PUT, "/farmers/**").hasRole("FARMER")
                    .requestMatchers(HttpMethod.DELETE, "/farmers/**").hasRole("FARMER")
                    .requestMatchers(HttpMethod.POST, "/dealers/**").hasRole("DEALER")
                    .requestMatchers(HttpMethod.PUT, "/dealers/**").hasRole("DEALER")
                    .requestMatchers(HttpMethod.DELETE, "/dealers/**").hasRole("DEALER")
                    .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
    }
}
