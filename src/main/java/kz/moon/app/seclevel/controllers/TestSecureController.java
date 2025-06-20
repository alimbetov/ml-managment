package kz.moon.app.seclevel.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/secure")
public class TestSecureController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from secured API!";
    }
}