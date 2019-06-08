package sk.rolandkortvely.cassovia.services;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import sk.rolandkortvely.cassovia.models.User;
import sk.rolandkortvely.cassovia.helpers.Hash;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("/test")
public class TestService {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    public JavaMailSender emailSender;

    @GET
    @Path("/crypt/{text}")
    @Produces("application/json")
    public ResponseEntity<String> crypt(@PathParam("text") String text) {
        return ResponseEntity.ok().body(Hash.make(text));
    }
}
