package application.services.security;

import application.repositories.IUserRepo;
import application.services.exceptions.UnauthorizedException;
import domain.entities.CarOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component("orderSecurity")
public class OrderSecurity {
    @Autowired
    private SecurityExtractor securityExtractor;

    @Autowired
    private IUserRepo userRepo;


    public boolean canSeeOrder(CarOrder order) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return false;
        }

        boolean elevatedRole = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SYSTEM_ADMIN")
                        || a.getAuthority().equals("ROLE_MANAGER"));

        if (elevatedRole) {
            return true;
        }

        var userKeycloakId = securityExtractor.getCurrentUserId();
        var user = userRepo.findByKeycloakId(UUID.fromString(userKeycloakId));

        if (user.isEmpty()) {
            throw new UnauthorizedException("no user");
        }

        return order.getUser().getId().equals(user.get().getId());
    }

    public List<CarOrder> canSeeOrderFilter(List<CarOrder> tickets) {
        return tickets.stream().filter(this::canSeeOrder).toList();
    }
}