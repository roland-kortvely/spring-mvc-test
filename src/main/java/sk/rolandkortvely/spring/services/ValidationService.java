package sk.rolandkortvely.spring.services;

import org.springframework.http.ResponseEntity;
import sk.rolandkortvely.spring.config.Service;
import sk.rolandkortvely.spring.models.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/validate")
public class ValidationService extends Service
{

    @GET
    @Path("/username/{text}")
    @Produces("application/json")
    public ResponseEntity<Boolean> crypt(@PathParam("text") String username)
    {
        return ResponseEntity.ok().body(
                new User()
                        .stream()
                        .filter(user -> user.getUsername().equals(username))
                        .findFirst().orElse(null) == null
        );
    }
}
