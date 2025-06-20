package kz.moon.app.seclevel.ui.view;



import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import kz.moon.app.seclevel.domain.*;
import kz.moon.app.seclevel.repository.RoleRepository;
import kz.moon.app.seclevel.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Route("register")
@PermitAll
public class RegistrationView extends VerticalLayout {

    public RegistrationView(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {

        TextField username = new TextField("Username");
        PasswordField password = new PasswordField("Password");

        Button registerBtn = new Button("Register", event -> {
            if (username.isEmpty() || password.isEmpty()) {
                Notification.show("Username and password required");
                return;
            }

            // по умолчанию даём роль USER
            Role userRole = roleRepository.findByName(RolesEnum.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found in roles table"));

            User newUser = User.builder()
                    .username(username.getValue())
                    .password(passwordEncoder.encode(password.getValue()))
                    .enabled(true)
                    .roles(Set.of(userRole))
                    .build();

            userRepository.save(newUser);

            Notification.show("User registered. You can login now.");
            username.clear();
            password.clear();
        });

        add(new H1("Register new user"), username, password, registerBtn);
        setMaxWidth("400px");
    }
}
