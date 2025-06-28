package kz.moon.app.seclevel.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.security.RolesAllowed;

@RestController
@RequestMapping("/api/admin/")
public class AdminRestController {

    @GetMapping("test")
    @RolesAllowed("ROLE_ADMIN")
    public String adminTest() {
        return "Hello from Admin REST controller!";
    }
}