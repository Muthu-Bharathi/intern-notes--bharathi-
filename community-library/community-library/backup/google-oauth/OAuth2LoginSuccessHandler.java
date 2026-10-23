package com.example.community_library.security;

import com.example.community_library.entity.AppUser;
import com.example.community_library.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

// Google login success aana udane user-ah DB-la save panni, JWT create panni cookie-la vachu Swagger-ku redirect panrom
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;

    public OAuth2LoginSuccessHandler(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
        setDefaultTargetUrl("/swagger-ui/index.html");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        AppUser user = userService.processGoogleUser(token.getPrincipal());

        // HttpOnly: JavaScript-ala token-ah padikka mudiyadhu. SameSite=Lax: vera site-la irundhu POST/PUT/DELETE-ku cookie pogadhu.
        ResponseCookie cookie = ResponseCookie.from(JwtService.ACCESS_TOKEN_COOKIE, jwtService.generateToken(user))
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(jwtService.getExpiration())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Google handshake-ku mattum dhaan session thevai; inime JWT dhaan
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
