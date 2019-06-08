package sk.rolandkortvely.cassovia.services;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import sk.rolandkortvely.cassovia.models.UserGroup;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;

@Path("/usergroup")
public class UserGroupService {

    @Autowired
    private SessionFactory sessionFactory;

    @GET
    @Path("/")
    @Produces("application/json")
    public ResponseEntity<List<UserGroup>> all() {
        return ResponseEntity.ok().body(UserGroup.all(sessionFactory));
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public ResponseEntity<UserGroup> get(@PathParam("id") int id) {
        return ResponseEntity.ok().body(UserGroup.find(sessionFactory, id));
    }
}
