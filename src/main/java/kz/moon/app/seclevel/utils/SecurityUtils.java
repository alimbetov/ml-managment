package kz.moon.app.seclevel.utils;

import kz.moon.app.seclevel.domain.RolesEnum;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class SecurityUtils {

    public static boolean hasRole(RolesEnum roleEnum) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority(roleEnum.name()));
    }
}
