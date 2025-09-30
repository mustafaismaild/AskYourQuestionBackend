package com.example.project.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher; // **Eklendi**

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF'yi modern lambda ile devre dışı bırak
                .csrf(AbstractHttpConfigurer::disable)

                // CORS yapılandırması için bean'i kullan
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // **Güncellendi**

                // HTTP istek yetkilendirme
                .authorizeHttpRequests(auth -> auth
                        // Public erişime açık endpoint'ler için AntPathRequestMatcher kullanıldı
                        .requestMatchers(
                                AntPathRequestMatcher.antMatcher("/api/auth/register"),
                                AntPathRequestMatcher.antMatcher("/api/auth/login"),
                                AntPathRequestMatcher.antMatcher("/v3/api-docs/**"),
                                AntPathRequestMatcher.antMatcher("/swagger-ui/**"),
                                AntPathRequestMatcher.antMatcher("/redoc.html"),
                                AntPathRequestMatcher.antMatcher("/favicon.ico")
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // Oturum yönetimini STATELESS olarak ayarla
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Güvenlik başlıkları yapılandırması
                .headers(headers -> headers
                        .xssProtection(xss -> xss.disable()) // XSS korumasını modern lambda ile devre dışı bırak
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; font-src 'self';")
                        )
                );

        // JWT filtresini ekle
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS yapılandırması
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // setAllowedOriginPatterns tercih edilir, ancak allowedOrigins da kullanılabilir
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000", // React dev server
                "http://localhost:3001"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 'Authorization' başlığını eklediğinizden emin olun
        configuration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));

        // Kimlik bilgisi (token/cookie) ile auth varsa bu önemlidir
        configuration.setAllowCredentials(true);

        // CORS yapılandırmasını tüm yollara uygula
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}