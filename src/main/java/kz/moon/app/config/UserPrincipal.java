package kz.moon.app.config;

import kz.moon.app.seclevel.domain.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class UserPrincipal extends org.springframework.security.core.userdetails.User {

    private final String displayName;

    public UserPrincipal(User user) {
        super(user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                        .toList());
        this.displayName = user.getUsername(); // или имя из твоей сущности
    }

    public String getDisplayName() {
        return displayName;
    }
}