package kz.moon.app.seclevel.ui.view;


import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

@Route("marker")
@Secured("ROLE_MARKER")
public class MarkingView extends VerticalLayout {

    public MarkingView() {
        add(new Span("Welcome to Martker zone!"));
    }
}
