package com.example.senior_project.config;

import com.example.senior_project.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/educational-contents/**",
                                "/api/v1/products/**",
                                "/api/v1/categories/**",
                                "/api/v1/ai/**",
                                "/api/v1/success-stories/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/*/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/buyer/comments/product/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/profiles/**").permitAll()
                        .requestMatchers("/static/**").permitAll()
                        // Chatbot endpoint - authenticated but no role required
                        .requestMatchers("/api/v1/chatbot/**").authenticated()
                        // Bildirimler ve Mesajlar
                        .requestMatchers("/api/v1/notifications/**").authenticated()
                        .requestMatchers("/api/v1/messages/**").authenticated()
                        // Comment endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/buyer/comments/*/reply").hasRole("SELLER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/buyer/comments").hasAnyRole("BUYER", "SELLER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/buyer/comments/**").hasAnyRole("BUYER", "SELLER")
                        // Buyer endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/buyer/follow/**").hasRole("BUYER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/buyer/unfollow/**").hasRole("BUYER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/buyer/following").hasRole("BUYER")
                        .requestMatchers("/api/v1/buyer/favorites/**").hasRole("BUYER")
                        .requestMatchers("/api/v1/cart/**").hasRole("BUYER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/cart/update/**").hasRole("BUYER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/buyer/orders").hasRole("BUYER")
                        // Seller endpoints
                        .requestMatchers("/api/v1/seller/**").hasRole("SELLER")
                        // Admin endpoints
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/ai/price-suggestion").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}