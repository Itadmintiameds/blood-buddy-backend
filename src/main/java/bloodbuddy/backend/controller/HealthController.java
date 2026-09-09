package bloodbuddy.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class HealthController {

    @GetMapping("/health-check")
    public String healthCheck() {
        return "Service is up and running";
    }
}
