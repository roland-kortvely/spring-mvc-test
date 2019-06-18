package sk.rolandkortvely.cassovia.services;

import org.springframework.http.ResponseEntity;
import sk.rolandkortvely.cassovia.models.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;

@Path("/user")
public class UserService extends Service {

    @GET
    @Produces("application/json")
    public ResponseEntity<List<User>> all() {
        return ResponseEntity.ok().body(new User().all());
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public ResponseEntity<User> get(@PathParam("id") int id) {
        return ResponseEntity.ok().body(new User().find(id));
    }
}
