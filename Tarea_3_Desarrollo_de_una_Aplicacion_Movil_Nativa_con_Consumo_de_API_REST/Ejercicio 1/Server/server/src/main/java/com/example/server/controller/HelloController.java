package com.example.server.controller;

import com.example.server.model.HelloResponse;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@CrossOrigin(origins = "*") // Enable CORS for all origins
public class HelloController {

    @GetMapping("/api/hello")
    public HelloResponse sayHello() {
        return new HelloResponse("¡Hola Mundo desde Spring Boot!");
    }
    
    // Catch-all mapping que redirige cualquier ruta a /api/hello
    @GetMapping("/**")
    public void handleAllRequests(HttpServletResponse response) throws IOException {
        response.sendRedirect("/api/hello");
    }
}
