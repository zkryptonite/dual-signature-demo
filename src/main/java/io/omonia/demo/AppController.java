package io.omonia.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {
    @GetMapping("/calc")
    public String home() {
        return "index";
    }

    @GetMapping("/items")
    public String items() {
        return "items";
    }
}
