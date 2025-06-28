package kz.moon.app.seclevel.controllers;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/marker")
public class MarkerRestController {

    @GetMapping("/test")
    @RolesAllowed("ROLE_MARKER")
    public String adminTest() {
        return "Hello from Marjer REST controller!";
    }
}