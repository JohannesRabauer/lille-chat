package dev.rabauer.lille_chat.frontend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

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
                .defaultSuccessUrl("/", true)
        );
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/logged-out")
                .permitAll()
        );
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/VAADIN/**"));
        return http.build();
    }
}
