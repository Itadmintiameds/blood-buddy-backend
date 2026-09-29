package bloodbuddy.backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	// ECS runs the container in UTC. The app is single-region (India), so pin the JVM
	// default timezone to IST so every LocalDateTime.now()/LocalDate.now() records IST
	// wall-clock time. The TZ=Asia/Kolkata env var in the ECS task definition is the
	// source of truth; this guard covers environments (local/dev/test) that miss it.
	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
	}

}
