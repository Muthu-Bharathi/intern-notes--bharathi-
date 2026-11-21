package com.example.community_library.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Component
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLES = "roles";

    private final String clientId;

    public KeycloakRoleConverter(@Value("${app.keycloak.client-id}") String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        for (String role : roles(jwt)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
        }
        return authorities;
    }

    public Set<String> roles(Jwt jwt) {
        Set<String> roles = new LinkedHashSet<>();
        addRoles(roles, jwt.getClaimAsMap("realm_access"));

        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null && resourceAccess.get(clientId) instanceof Map<?, ?> clientAccess) {
            addRoles(roles, clientAccess);
        }
        return roles;
    }

    private void addRoles(Set<String> roles, Map<?, ?> access) {
        if (access != null && access.get(ROLES) instanceof List<?> list) {
            list.forEach(role -> roles.add(String.valueOf(role)));
        }
    }
}
