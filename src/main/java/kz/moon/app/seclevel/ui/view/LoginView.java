package kz.moon.app.seclevel.ui.view;

import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.router.Route;

@Route("login")
public class LoginView extends VerticalLayout {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        loginForm.setAction("/login");

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        Anchor registerLink = new Anchor("register", "Зарегистрироваться");
        registerLink.getStyle().set("margin-top", "1em");
        registerLink.getStyle().set("text-decoration", "underline");
        registerLink.getStyle().set("color", "var(--lumo-primary-text-color)");

        add(loginForm, registerLink);
    }
}
