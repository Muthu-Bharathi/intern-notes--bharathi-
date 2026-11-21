package com.example.community_library.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Swagger "Authorize" button: "keycloak" - Keycloak login page-ku pogum; "bearerAuth" - already irukkura JWT paste pannalam.
@Configuration
public class OpenApiConfig {

    private static final String KEYCLOAK_SCHEME = "keycloak";
    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI communityLibraryOpenAPI(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        String openIdConnect = issuerUri + "/protocol/openid-connect";

        SecurityScheme keycloak = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Login with Keycloak")
                .flows(new OAuthFlows().authorizationCode(new OAuthFlow()
                        .authorizationUrl(openIdConnect + "/auth")
                        .tokenUrl(openIdConnect + "/token")
                        .scopes(new Scopes().addString("openid", "User identity"))));

        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Paste a Keycloak access token");

        return new OpenAPI()
                .info(new Info()
                        .title("Community Library API")
                        .description("Click <b>Authorize</b> to log in with Keycloak. "
                                + "Roles (kept in Keycloak): ADMIN &gt; LIBRARIAN &gt; MEMBER. "
                                + "Call <code>/api/auth/me</code> to see the user and roles read from your token."))
                .components(new Components()
                        .addSecuritySchemes(KEYCLOAK_SCHEME, keycloak)
                        .addSecuritySchemes(BEARER_SCHEME, bearer))
                .addSecurityItem(new SecurityRequirement().addList(KEYCLOAK_SCHEME))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
