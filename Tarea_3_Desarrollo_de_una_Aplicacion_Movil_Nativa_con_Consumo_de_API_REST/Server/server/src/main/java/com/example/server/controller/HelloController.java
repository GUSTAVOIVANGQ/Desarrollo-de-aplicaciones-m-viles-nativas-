package com.example.server.controller;

import com.example.server.model.HelloResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public HelloResponse sayHello() {
        return new HelloResponse("¡Hola Mundo desde Spring Boot!");
    }
}
