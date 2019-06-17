package sk.rolandkortvely.cassovia.services;

import org.springframework.http.ResponseEntity;
import sk.rolandkortvely.cassovia.models.QueryStream.Order;
import sk.rolandkortvely.cassovia.models.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.stream.Collectors;

@Path("/test")
@Produces("application/json")
public class TestService extends Service {

    @GET
    public ResponseEntity<List> test() {

        List<User> r = User.query(sessionFactory)
                .orderBy("id", Order.DESC)
                .stream().collect(Collectors.toList());

        return ResponseEntity.ok().body(r);
    }
}
