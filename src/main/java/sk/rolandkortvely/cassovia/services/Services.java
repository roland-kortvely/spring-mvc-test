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

    @Bean(name = "userGroupService")
    public UserGroupService userGroupService() {
        return new UserGroupService();
    }

    @Bean(name = "userService")
    public UserService userService() {
        return new UserService();
    }
}
