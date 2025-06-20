package kz.moon.app.seclevel.repository;


import kz.moon.app.seclevel.domain.Role;
import kz.moon.app.seclevel.domain.RolesEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RolesEnum name);
}
