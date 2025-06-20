package kz.moon.app.seclevel.ui.view;


import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

@Route("expert")
@Secured("ROLE_EXPERT")
public class ExpertToolsView extends VerticalLayout {

    public ExpertToolsView() {
        add(new Span("Welcome to ExpertToolsView zone!"));
    }
}
