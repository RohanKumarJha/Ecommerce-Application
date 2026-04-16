package ecommerce.user.repository;

import ecommerce.user.model.ENUM.AppRole;
import ecommerce.user.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(AppRole appRole);
}