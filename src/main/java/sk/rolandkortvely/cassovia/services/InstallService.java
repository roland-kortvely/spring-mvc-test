package sk.rolandkortvely.cassovia.services;

import org.springframework.http.ResponseEntity;
import sk.rolandkortvely.cassovia.helpers.Hash;
import sk.rolandkortvely.cassovia.models.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Fill the database with default User
 */
@Path("/install")
public class InstallService extends AbstractService {

    /**
     * Fill the database with default User
     *
     * @return Generated User
     */
    @GET
    @Path("/")
    @Produces("application/json")
    public ResponseEntity<User> install() {

        /*
         * Creates default User "admin" with "admin" role
         */
        User user = User.stream(sessionFactory)
                .filter(u -> u.getUsername().equals("admin"))
                .findFirst().orElse(null);
        if (user == null) {
            user = new User(sessionFactory);
            user.setUsername("admin");
            user.setPassword(Hash.make("s3cur3"));
            user.setEmail("admin@test.com");
            user.setAdmin();
            user.save();
        }

        /*
         * Returns default user
         */
        return ResponseEntity.ok().body(user);
    }
}
