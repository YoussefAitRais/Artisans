package org.event.backend.config;

import org.event.backend.service.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

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

                // Enable CORS to use the bean defined below
                .cors(cors -> {})

                .authorizeHttpRequests(auth -> auth
                        // Allow CORS preflight
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

                        // Quotes: client can accept a quote (specific BEFORE generic)
                        .requestMatchers(HttpMethod.POST, "/api/quotes/*/accept").hasRole("CLIENT")

                        // Quotes: artisan creates/updates/deletes, client lists
                        .requestMatchers(HttpMethod.POST, "/api/requests/**/quotes").hasRole("ARTISAN")
                        .requestMatchers(HttpMethod.GET,  "/api/requests/**/quotes").hasRole("CLIENT")
                        .requestMatchers("/api/quotes/**").hasRole("ARTISAN")

                        // Engagements & Conversations
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

    // Global CORS configuration (reads allowed origins from properties)
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:4200}") String originsProp) {

        List<String> origins = Arrays.stream(originsProp.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        CorsConfiguration cfg = new CorsConfiguration();
        // With credentials=true, you must not use "*"
        cfg.setAllowedOrigins(origins);
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With","Accept","Origin"));
        // Expose headers if you return them (e.g., Location, Content-Disposition for downloads)
        cfg.setExposedHeaders(List.of("Location","Content-Disposition"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
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
