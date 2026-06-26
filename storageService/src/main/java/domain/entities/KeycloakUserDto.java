package domain.entities;

import java.util.List;
import java.util.Map;

public record KeycloakUserDto(
        String id,
        String username,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        Map<String, List<String>> attributes
) {}
