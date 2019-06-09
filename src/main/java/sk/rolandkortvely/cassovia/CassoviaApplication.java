package sk.rolandkortvely.cassovia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

/**
 * Server application for CRUD management of given resources
 */
@SpringBootApplication
@Configuration
public class CassoviaApplication {

    /**
     * Run Server
     * To fill the database with default data, go to /api/install
     *
     * @param args app arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CassoviaApplication.class, args);
    }
}
