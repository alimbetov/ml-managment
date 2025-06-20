package kz.moon.app.seclevel.ui.view;


import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

@Route("admin")
@Secured("ROLE_ADMIN")
public class AdminView extends VerticalLayout {

    public AdminView() {
        add(new Span("Welcome to Admin zone!"));
    }
}
