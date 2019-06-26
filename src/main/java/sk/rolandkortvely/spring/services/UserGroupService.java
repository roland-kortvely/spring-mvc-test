package sk.rolandkortvely.spring.services;

import org.springframework.http.ResponseEntity;
import sk.rolandkortvely.spring.config.Service;
import sk.rolandkortvely.spring.models.UserGroup;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;

@Path("/usergroup")
public class UserGroupService extends Service
{

    @GET
    @Produces("application/json")
    public ResponseEntity<List<UserGroup>> all()
    {
        return ResponseEntity.ok().body(new UserGroup().all());
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public ResponseEntity<UserGroup> get(@PathParam("id") int id)
    {
        return ResponseEntity.ok().body(new UserGroup().find(id));
    }
}
