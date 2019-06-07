package sk.samuelkarabas.cassovia.services;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import sk.samuelkarabas.cassovia.entities.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;

@Path("/user")
public class UserService {

    @Autowired
    private SessionFactory sessionFactory;

    @GET
    @Path("/")
    @Produces("application/json")
    public ResponseEntity<List<User>> all() {
        return ResponseEntity.ok().body(User.all(sessionFactory));
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public ResponseEntity<User> get(@PathParam("id") int id) {
        return ResponseEntity.ok().body(User.find(sessionFactory, id));
    }
}
