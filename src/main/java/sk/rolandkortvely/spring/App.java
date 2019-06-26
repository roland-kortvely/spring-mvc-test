package sk.rolandkortvely.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

/**
 * Server application for CRUD management of given resources
 */
@SpringBootApplication
@Configuration
public class App
{

    /**
     * Run Server
     * To fill the database with default data, go to /install
     *
     * @param args app arguments
     */
    public static void main(String[] args)
    {
        SpringApplication.run(App.class, args);
    }


    /*
    /**
     * @return Client application
     */
    /*
    @Bean
    public ApplicationRunner applicationRunner()
    {
        return args -> null;
    }
    */
}
