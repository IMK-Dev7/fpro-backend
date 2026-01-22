package com.facturation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Autoriser tous les domaines Vercel (y compris les preview deployments)
        config.addAllowedOrigin("https://md-best-quality.vercel.app");
        config.addAllowedOriginPattern("https://*.vercel.app"); // Tous les sous-domaines Vercel
        config.addAllowedOriginPattern("https://*-git-*-imk-dev7.vercel.app"); // URLs de preview
        
        // Développement local
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://127.0.0.1:5173");
        config.addAllowedOrigin("http://127.0.0.1:3000");

        // Autoriser les headers nécessaires
        config.addAllowedHeader("*");

        // Autoriser les méthodes HTTP
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("PATCH");
        config.addAllowedMethod("HEAD");

        // Autoriser les credentials (cookies, auth headers)
        config.setAllowCredentials(true);

        // Headers exposés
        config.addExposedHeader("Content-Disposition"); // Important pour les PDF
        config.addExposedHeader("Content-Type");
        config.addExposedHeader("Authorization");

        // Max age pour le preflight (1 heure)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
