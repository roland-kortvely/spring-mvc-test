package sk.rolandkortvely.cassovia.services;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import sk.rolandkortvely.cassovia.entities.User;
import sk.rolandkortvely.cassovia.helpers.Hash;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/test")
public class TestService {

    @Autowired
    private SessionFactory sessionFactory;

    @GET
    @Path("/")
    @Produces("application/json")
    public ResponseEntity<User> test() {
        return ResponseEntity.ok().body(
                User.first(sessionFactory)
        );
    }

    @GET
    @Path("/crypt/{text}")
    @Produces("application/json")
    public ResponseEntity<String> crypt(@PathParam("text") String text) {
        return ResponseEntity.ok().body(Hash.make(text));
    }
}
