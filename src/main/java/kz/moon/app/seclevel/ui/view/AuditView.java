package kz.moon.app.seclevel.ui.view;


import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

@Route("audit")
@Secured("ROLE_AUDIT")
public class AuditView extends VerticalLayout {

    public AuditView() {
        add(new Span("Welcome to Audit zone!"));
    }
}
