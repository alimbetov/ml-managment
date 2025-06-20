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
import kz.moon.app.taskmanagement.ui.view.TaskListView;
import kz.moon.app.seclevel.ui.view.AdminView;
import kz.moon.app.seclevel.ui.view.MarkingView;
import kz.moon.app.seclevel.ui.view.AuditView;
import kz.moon.app.seclevel.ui.view.ExpertToolsView;
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

        // Добавляем в "боковую панель":
        VerticalLayout drawerContent = new VerticalLayout(header, sideNav, logoutButton);
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.setSizeFull();
        drawerContent.expand(sideNav);

        addToDrawer(drawerContent);
        addToNavbar(userMenu);
    }

    private Div createHeader() {
        var appLogo = VaadinIcon.CUBES.create();
        appLogo.addClassNames(TextColor.PRIMARY, IconSize.LARGE);

        var appName = new Span("ML Annotation Tracker");
        appName.addClassNames(FontWeight.SEMIBOLD, FontSize.LARGE);

        var header = new Div(appLogo, appName);
        header.addClassNames(Display.FLEX, Padding.MEDIUM, Gap.MEDIUM, AlignItems.CENTER);
        return header;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.addClassNames(Margin.Horizontal.MEDIUM);

        // Всегда доступно
        nav.addItem(new SideNavItem("Task List", "task-list", VaadinIcon.CLIPBOARD_CHECK.create()));
        // Только для ROLE_EXPERT
        if (SecurityUtils.hasRole(RolesEnum.ROLE_USER)) {
            nav.addItem(new SideNavItem("Expert Tools", "expert", VaadinIcon.TOOLS.create()));
        }
        // Только для ROLE_ADMIN
        if (SecurityUtils.hasRole(RolesEnum.ROLE_ADMIN)) {
            nav.addItem(new SideNavItem("Admin Panel", "admin", VaadinIcon.COG.create()));
        }

        // Только для ROLE_MARKER
        if (SecurityUtils.hasRole(RolesEnum.ROLE_MARKER)) {
            nav.addItem(new SideNavItem("Marking Tasks", "marker", VaadinIcon.EDIT.create()));
        }

        // Только для ROLE_AUDIT
        if (SecurityUtils.hasRole(RolesEnum.ROLE_AUDIT)) {
            nav.addItem(new SideNavItem("Audit Panel", "audit", VaadinIcon.SEARCH.create()));
        }

        // Только для ROLE_EXPERT
        if (SecurityUtils.hasRole(RolesEnum.ROLE_EXPERT)) {
            nav.addItem(new SideNavItem("Expert Tools", "expert", VaadinIcon.TOOLS.create()));
        }

        return nav;
    }

    private Component createUserMenu() {
        // Получаем текущего пользователя
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

        // Теперь создаём Avatar и Menu
        var avatar = new Avatar(displayName);
        avatar.addThemeVariants(AvatarVariant.LUMO_XSMALL);
        avatar.addClassNames(Margin.Right.SMALL);
        avatar.setColorIndex(5);

        var userMenu = new MenuBar();
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
