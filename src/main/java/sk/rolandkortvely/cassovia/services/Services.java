package sk.rolandkortvely.cassovia.services;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.ws.rs.ApplicationPath;

@Configuration
@ApplicationPath("/api")
public class Services extends ResourceConfig {

    public Services() {
        packages("sk.rolandkortvely.cassovia");
    }

    /**
     * Comment after successful installation
     *
     * @return Service for default installation
     */
    @Bean
    public InstallService installService() {
        return new InstallService();
    }

    @Bean
    public CryptoService cryptoService() {
        return new CryptoService();
    }

    @Bean
    public TestService testService() {
        return new TestService();
    }

    @Bean
    public ValidationService validationService() {
        return new ValidationService();
    }

    @Bean
    public UserGroupService userGroupService() {
        return new UserGroupService();
    }

    @Bean
    public UserService userService() {
        return new UserService();
    }
}
