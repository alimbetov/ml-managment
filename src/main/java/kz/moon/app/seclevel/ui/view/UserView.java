package kz.moon.app.seclevel.ui.view;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

@Route("user")
@Secured({"ROLE_USER", "ROLE_MANAGER"})
public class UserView extends VerticalLayout {

    public UserView() {
        add(new Span("Welcome, User or Manager!"));
    }
}