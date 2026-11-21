package com.example.community_library.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Keycloak realm roles
    private static final String ADMIN = "ADMIN";
    private static final String LIBRARIAN = "LIBRARIAN";
    private static final String MEMBER = "MEMBER";

    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(KeycloakRoleConverter keycloakRoleConverter) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(keycloakRoleConverter);
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }

    // ADMIN can do everything a LIBRARIAN can; LIBRARIAN can do everything a MEMBER can
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(ADMIN).implies(LIBRARIAN)
                .role(LIBRARIAN).implies(MEMBER)
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/books/**").hasRole(MEMBER)
                        .requestMatchers("/api/books/**").hasRole(LIBRARIAN)
                        .requestMatchers("/api/members/**").hasRole(LIBRARIAN)
                        .requestMatchers(HttpMethod.GET, "/api/loans/**").hasRole(MEMBER)
                        .requestMatchers("/api/loans/**").hasRole(LIBRARIAN)
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(resource -> resource
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }
}
