package com.github.pricemonitor.config;

import com.github.pricemonitor.properties.AppProperties;
import com.github.pricemonitor.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AppProperties appProperties;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                ).addFilterBefore(this.jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(List.of(this.appProperties.clientUrl()));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setAllowCredentials(true);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);

        return source;
    }

}
