package application.services.security;

import application.repositories.IUserRepo;
import application.repositories.rows.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSyncService {
    List<String> skipRoles = List.of("default-roles-lab_realm", "offline_access", "uma_authorization");

    @Autowired
    private final IUserRepo userRepository;

    @Transactional
    public void syncUser(Jwt jwt) {
        UUID keycloakId = UUID.fromString(jwt.getSubject());
        String username = jwt.getClaimAsString("preferred_username");
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null || realmAccess.get("roles") == null) {
            return;
        }

        var user = userRepository.findByKeycloakId(keycloakId);
        if (user.isEmpty()) {
            @SuppressWarnings("unchecked")
            String role = ((List<String>) realmAccess.get("roles")).stream().filter(x -> !skipRoles.contains(x)).toList().getFirst();

            if (role.isEmpty()) {
                return;
            }

            User newUser = new User(keycloakId, username, role);
            userRepository.save(newUser);
        } else if (user.get().getKeycloakId() == null) {
            user.get().setKeycloakId(keycloakId);

            userRepository.save(user.get());
        }
    }
}