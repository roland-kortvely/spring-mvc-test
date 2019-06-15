package sk.rolandkortvely.cassovia.services;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import sk.rolandkortvely.cassovia.models.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/validate")
public class ValidationService extends AbstractService {

    /**
     * Database context (MySQL)
     */
    @Autowired
    protected SessionFactory sessionFactory;

    @GET
    @Path("/username/{text}")
    @Produces("application/json")
    public ResponseEntity<Boolean> crypt(@PathParam("text") String username) {
        return ResponseEntity.ok().body(User
                .stream(sessionFactory)
                .filter(user -> user.getUsername().equals(username))
                .findFirst().orElse(null) == null
        );
    }
}
