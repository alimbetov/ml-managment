package kz.moon.app.seclevel.services;

import jakarta.annotation.PostConstruct;
import kz.moon.app.seclevel.domain.Role;
import kz.moon.app.seclevel.domain.RolesEnum;
import kz.moon.app.seclevel.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleInitializer {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void initRoles() {
        for (RolesEnum roleEnum : RolesEnum.values()) {
            roleRepository.findByName(roleEnum)
                    .orElseGet(() -> {
                        Role newRole = Role.builder()
                                .name(roleEnum)
                                .build();
                        roleRepository.save(newRole);
                        System.out.println("Created role: " + roleEnum);
                        return newRole;
                    });
        }
    }
}