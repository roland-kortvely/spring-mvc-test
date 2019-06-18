package sk.rolandkortvely.cassovia.services;

import org.springframework.http.ResponseEntity;
import sk.rolandkortvely.cassovia.models.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/test")
@Produces("application/json")
public class TestService extends Service {

    @GET
    public ResponseEntity<User> test() {
        return ResponseEntity.ok().body(auth(session));
    }
}
