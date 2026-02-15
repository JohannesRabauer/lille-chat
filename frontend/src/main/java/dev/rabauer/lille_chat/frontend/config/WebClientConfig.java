package dev.rabauer.lille_chat.frontend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
class WebClientConfig {

    @Value("${lille-chat.backend-url}")
    private String backendUrl;

    @Bean
    WebClient webClient(OAuth2AuthorizedClientRepository authorizedClientRepository,
                        ClientRegistrationRepository clientRegistrationRepository) {
        var oauth2 = new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                clientRegistrationRepository, authorizedClientRepository);
        oauth2.setDefaultClientRegistrationId("keycloak");
        return WebClient.builder()
                .baseUrl(backendUrl)
                .apply(oauth2.oauth2Configuration())
                .build();
    }
}
