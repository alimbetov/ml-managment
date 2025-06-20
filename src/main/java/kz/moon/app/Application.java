package kz.moon.app;

import com.vaadin.flow.component.page.AppShellConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class Application implements AppShellConfigurator {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
