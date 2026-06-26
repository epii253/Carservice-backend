package application.repositories.rows;

import domain.entities.BaseEntity;
import domain.valueObjects.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.util.UUID;

@Entity(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name = "user_name")
    private String name;

    @Column(
            name = "keycloak_id",
            nullable = true,
            unique = true
    )
    @Setter
    private UUID keycloakId;

    public User(
            @NonNull
            UUID keycloakId,
            @NonNull
            String name,
            @NonNull
            String role
    ) {
        this.keycloakId = keycloakId;
        this.name = name;
        this.role = Role.valueOf(role);
    }
}
