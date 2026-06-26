package application.services;

import domain.entities.KeycloakUserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class KeycloakService {
    private final RestClient keycloakRestClientConfig;

    @Value("${keycloak.role-path}")
    private String getRolesPrefixPath;

    public KeycloakService(RestClient keycloakRestClientConfig) {
        this.keycloakRestClientConfig = keycloakRestClientConfig;
    }

    public List<KeycloakUserDto> getUsersByRole(String roleName) {
        return keycloakRestClientConfig.get()
                .uri(getRolesPrefixPath + "{role}/users", roleName)
                .retrieve()
                .body(new ParameterizedTypeReference<List<KeycloakUserDto>>() {});
    }
}