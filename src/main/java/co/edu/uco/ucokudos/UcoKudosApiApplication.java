package co.edu.uco.ucokudos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UcoKudosApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(UcoKudosApiApplication.class, args);
    }
}
