package carrent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RentCarAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentCarAppApplication.class, args);
    }

}
