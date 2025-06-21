package kz.moon.app.base.ui.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import jakarta.annotation.security.PermitAll;
import kz.moon.app.config.UserPrincipal;
import kz.moon.app.seclevel.domain.RolesEnum;
import kz.moon.app.seclevel.utils.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.vaadin.flow.theme.lumo.LumoUtility.*;

@Layout
@PermitAll
public final class MainLayout extends AppLayout {

    public MainLayout() {
        setPrimarySection(Section.DRAWER);

        SideNav sideNav = createSideNav();
        Component header = createHeader();
        Component userMenu = createUserMenu();
        Button logoutButton = createLogoutButton();

        VerticalLayout drawerContent = new VerticalLayout(header, sideNav, logoutButton);
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.setSizeFull();
        drawerContent.expand(sideNav);

        addToDrawer(drawerContent);
        addToNavbar(userMenu);
    }

    private Div createHeader() {
        Icon appLogo = VaadinIcon.CUBES.create();
        appLogo.addClassNames(TextColor.PRIMARY, IconSize.LARGE);

        Span appName = new Span("ML Annotation Tracker");
        appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);

        Div header = new Div(appLogo, appName);
        header.addClassNames(Display.FLEX, Padding.MEDIUM, Gap.MEDIUM, AlignItems.CENTER);
        return header;
    }

    private SideNav createSideNav() {
        SideNav nav = new SideNav();
        nav.addClassNames(Margin.Horizontal.MEDIUM);

        // Страницы — централизованно
        addAlwaysVisible(nav);
        addAdminPages(nav);
        addExpertPages(nav);
        addMarkerPages(nav);
        addAuditPages(nav);

        return nav;
    }

    private void addAlwaysVisible(SideNav nav) {
        nav.addItem(new SideNavItem("Task List", "task-list", VaadinIcon.CLIPBOARD_CHECK.create()));

        // Добавляем основные страницы
        nav.addItem(new SideNavItem("Projects", "project-list", VaadinIcon.PACKAGE.create()));
        nav.addItem(new SideNavItem("Classifiers", "classifier-list", VaadinIcon.LAYOUT.create()));
        nav.addItem(new SideNavItem("Categories", "category-list", VaadinIcon.TAGS.create()));
        nav.addItem(new SideNavItem("Images", "image-list", VaadinIcon.PICTURE.create()));
        nav.addItem(new SideNavItem("Annotations", "annotation-list", VaadinIcon.FILE_TEXT.create()));
        nav.addItem(new SideNavItem("Assignments", "assignment-list", VaadinIcon.USERS.create()));
    }

    private void addAdminPages(SideNav nav) {
        if (SecurityUtils.hasRole(RolesEnum.ROLE_ADMIN)) {
            nav.addItem(new SideNavItem("Admin Panel", "admin", VaadinIcon.COG.create()));
        }
    }

    private void addExpertPages(SideNav nav) {
        if (SecurityUtils.hasRole(RolesEnum.ROLE_EXPERT)) {
            nav.addItem(new SideNavItem("Expert Tools", "expert", VaadinIcon.TOOLS.create()));
        }
    }

    private void addMarkerPages(SideNav nav) {
        if (SecurityUtils.hasRole(RolesEnum.ROLE_MARKER)) {
            nav.addItem(new SideNavItem("Marking Tasks", "marker", VaadinIcon.EDIT.create()));
        }
    }

    private void addAuditPages(SideNav nav) {
        if (SecurityUtils.hasRole(RolesEnum.ROLE_AUDIT)) {
            nav.addItem(new SideNavItem("Audit Panel", "audit", VaadinIcon.SEARCH.create()));
        }
    }

    private Component createUserMenu() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String displayName = "Unknown";
        String roles = "No Roles";

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal userPrincipal) {
                displayName = userPrincipal.getDisplayName();
            }

            roles = authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .reduce((r1, r2) -> r1 + ", " + r2)
                    .orElse("No Roles");
        }

        Avatar avatar = new Avatar(displayName);
        avatar.addThemeVariants(AvatarVariant.LUMO_XSMALL);
        avatar.addClassNames(Margin.Right.SMALL);
        avatar.setColorIndex(5);

        MenuBar userMenu = new MenuBar();
        userMenu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        userMenu.addClassNames(Margin.MEDIUM);

        var userMenuItem = userMenu.addItem(avatar);
        userMenuItem.add(displayName + " (" + roles + ")");

        userMenuItem.getSubMenu().addItem("View Profile").setEnabled(false);
        userMenuItem.getSubMenu().addItem("Manage Settings").setEnabled(false);
        userMenuItem.getSubMenu().addItem("Logout", e -> {
            getUI().ifPresent(ui -> ui.getPage().setLocation("/logout"));
        });

        return userMenu;
    }

    private Button createLogoutButton() {
        Button logoutBtn = new Button("Logout", event -> {
            getUI().ifPresent(ui -> ui.getPage().setLocation("/logout"));
        });
        logoutBtn.addClassNames(Margin.MEDIUM, Width.FULL);
        return logoutBtn;
    }
}
