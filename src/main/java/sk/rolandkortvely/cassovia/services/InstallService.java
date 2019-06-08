package sk.rolandkortvely.cassovia.services;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import sk.rolandkortvely.cassovia.helpers.Hash;
import sk.rolandkortvely.cassovia.models.User;
import sk.rolandkortvely.cassovia.models.UserGroup;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Fill the database with default UserGroup and User
 */
@Path("/install")
public class InstallService {

    /**
     * Database context (MySQL)
     */
    @Autowired
    private SessionFactory sessionFactory;

    /**
     * Fill the database with default UserGroup and User
     *
     * @return Generated User
     */
    @GET
    @Path("/")
    @Produces("application/json")
    public ResponseEntity<User> install() {

        /*
         * Creates default UserGroup "admin"
         */
        UserGroup group = UserGroup.stream(sessionFactory)
                .filter(userGroup -> userGroup.getGroupName().equals("admin"))
                .findFirst().orElse(null);
        if (group == null) {
            group = new UserGroup(sessionFactory);
            group.setGroupName("admin");
            group.save();
        }

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
            user.setRole(group);
            user.save();
        }

        /*
         * Returns default user
         */
        return ResponseEntity.ok().body(user);
    }
}
