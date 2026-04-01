package com.josefuentes4096.products.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

// Esta configuración NO carga en producción (perfil 'prod').
// En producción, Spring Security aplica su configuración por defecto (autenticación requerida).
// Para producción real: crear un SecurityConfig con @Profile("prod") y reglas por rol.
@Profile("!prod")
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // DESARROLLO: CSRF deshabilitado para API REST stateless; todos los endpoints son públicos.
    // En producción reemplazar .permitAll() por reglas de autorización por rol.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
