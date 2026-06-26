package application.services.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestClient;

@Configuration
public class KeycloakRestClientConfig {

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${spring.security.oauth2.client.registration.storage-client.client-id}")
    private String servicePrincipalName;

    private final String registrationId = "storage-client";

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService clientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, clientService);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean
    public RestClient keycloakClient(
            RestClient.Builder builder,
            OAuth2AuthorizedClientManager authorizedClientManager) {

        return builder
                .baseUrl(keycloakUrl)
                .requestInterceptor((request, body, execution) -> {
                try {
                    OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                            .withClientRegistrationId(registrationId)
                            .principal(servicePrincipalName)
                            .build();

                    OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

                    if (authorizedClient != null) {
                        String token = authorizedClient.getAccessToken().getTokenValue();
                        request.getHeaders().setBearerAuth(token);
                    }

                    return execution.execute(request, body);
                } catch (Exception e) {
                    System.err.println("!!! REST CLIENT ERROR: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
                })
                .build();
    }
}