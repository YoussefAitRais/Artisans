package org.event.backend.config;

import org.event.backend.service.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Return JSON for 401/403 instead of HTML
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authEx) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.getWriter().write("""
                        {"status":401,"error":"Unauthorized","message":"Authentication required","path":"%s"}
                        """.formatted(request.getRequestURI()));
                        })
                        .accessDeniedHandler((request, response, denEx) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write("""
                        {"status":403,"error":"Forbidden","message":"You do not have permission to access this resource","path":"%s"}
                        """.formatted(request.getRequestURI()));
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        // Allow CORS preflight if needed
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public portfolio & media files
                        .requestMatchers(HttpMethod.GET, "/api/artisans/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/media/**").permitAll()

                        // Auth endpoints
                        .requestMatchers(
                                "/api/auth/register-client",
                                "/api/auth/register-artisan",
                                "/api/auth/login",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()

                        // Quotes: client can accept a quote (more specific BEFORE generic)
                        .requestMatchers(HttpMethod.POST, "/api/quotes/*/accept").hasRole("CLIENT")

                        // Quotes: artisan creates/updates/deletes, client lists quotes of own request
                        .requestMatchers(HttpMethod.POST, "/api/requests/**/quotes").hasRole("ARTISAN")
                        .requestMatchers(HttpMethod.GET,  "/api/requests/**/quotes").hasRole("CLIENT")
                        .requestMatchers("/api/quotes/**").hasRole("ARTISAN")

                        // Engagements & Conversations (both client/artisan)
                        .requestMatchers("/api/engagements/**").hasAnyRole("CLIENT","ARTISAN")
                        .requestMatchers("/api/conversations/**").hasAnyRole("CLIENT","ARTISAN")

                        // Role-based areas
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/artisan/**").hasRole("ARTISAN")
                        .requestMatchers("/api/client/**").hasRole("CLIENT")

                        // Everything else requires auth
                        .anyRequest().authenticated()
                )

                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt hashing
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
