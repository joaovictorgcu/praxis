package school.cesar.praxis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PraxisApplication {

    public static void main(String[] args) {
        SpringApplication.run(PraxisApplication.class, args);
    }
}
