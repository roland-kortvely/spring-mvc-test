package sk.rolandkortvely.spring.services;

import org.springframework.http.ResponseEntity;
import sk.rolandkortvely.spring.config.Service;
import sk.rolandkortvely.spring.helpers.Hash;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * Crypto functions for development purposes
 */
@Path("/crypto")
public class CryptoService extends Service
{

    /**
     * Generated hash for the given String
     *
     * @param text String to be hashed
     * @return Hash of the given String
     */
    @GET
    @Path("/hash/{text}")
    @Produces("application/json")
    public ResponseEntity<String> crypt(@PathParam("text") String text)
    {
        return ResponseEntity.ok().body(Hash.make(text));
    }
}
