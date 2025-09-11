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
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

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

    @Bean(name = "corsFilter")
    public CorsFilter corsFilter(
            @Value("${app.cors.allowed-origins:http://localhost:4200}") String originsProp
    ) {
        List<String> origins = Arrays.stream(originsProp.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(origins);
        cfg.setAllowCredentials(true);
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Location","Content-Disposition","Authorization"));
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(source);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(cors -> {})

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
                        // Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Public GETs
                        .requestMatchers(HttpMethod.GET, "/api/artisans/**", "/api/media/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()

                        // Auth & docs
                        .requestMatchers(
                                "/api/auth/register-client",
                                "/api/auth/register-artisan",
                                "/api/auth/login",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()


                        .requestMatchers(HttpMethod.POST, "/api/requests/*/quotes").hasRole("ARTISAN")
                        .requestMatchers(HttpMethod.PUT,    "/api/quotes/**").hasRole("ARTISAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/quotes/**").hasRole("ARTISAN")
                        .requestMatchers(HttpMethod.POST, "/api/quotes/*/accept").hasRole("CLIENT")

                        .requestMatchers("/api/requests/**").hasRole("CLIENT")
                        .requestMatchers("/api/artisan/requests/**").hasRole("ARTISAN")
                        .requestMatchers("/api/admin/requests/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/artisan/**").hasRole("ARTISAN")
                        .requestMatchers("/api/client/**").hasRole("CLIENT")

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
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean public WebSecurityCustomizer webSecurityCustomizer()
    { return web -> web.ignoring().requestMatchers(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/webjars/**",
            "/favicon.ico",
            "/error" );
    }
}
