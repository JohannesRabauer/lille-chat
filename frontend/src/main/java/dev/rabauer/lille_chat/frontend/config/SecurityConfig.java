package dev.rabauer.lille_chat.frontend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

@Configuration
class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/VAADIN/**", "/vaadinServlet/**", "/frontend/**",
                        "/icons/**", "/images/**", "/sw.js", "/offline.html",
                        "/manifest.webmanifest", "/sw-runtime-resources-precache.js",
                        "/logout", "/logged-out").permitAll()
                .anyRequest().authenticated()
        );
        http.oauth2Login(oauth2 -> oauth2
                .successHandler(new SavedRequestAwareAuthenticationSuccessHandler() {
                    {
                        setDefaultTargetUrl("/");
                        setAlwaysUseDefaultTargetUrl(true);
                    }
                })
                .userInfoEndpoint(userInfo -> userInfo
                        .oidcUserService(oidcUserService())
                )
        );
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/logged-out")
                .permitAll()
        );
        http.csrf(csrf -> csrf.ignoringRequestMatchers(
                request -> request.getRequestURI().startsWith("/VAADIN/") ||
                           request.getParameter("v-r") != null  // Vaadin UIDL requests
        ));
        return http.build();
    }

    /**
     * Custom OIDC user service that skips the userinfo endpoint call.
     * Uses only the ID token claims since Keycloak includes all necessary info there.
     */
    @Bean
    OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        return userRequest -> {
            var idToken = userRequest.getIdToken();
            return new DefaultOidcUser(
                    java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
                    idToken,
                    "preferred_username"
            );
        };
    }
}
