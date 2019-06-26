package sk.rolandkortvely.spring.config;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import sk.rolandkortvely.spring.models.User;
import sk.rolandkortvely.spring.traits.Auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Abstract Service to extend Services with useful methods
 */
@Component
public abstract class Service implements Auth
{

    /**
     * Instance of Mail, connection to SMTP server to send emails
     */
    @Qualifier("getJavaMailSender")
    @Autowired
    public JavaMailSender emailSender;
    /**
     * Database context (MySQL)
     */
    @Autowired
    protected SessionFactory sessionFactory;

    /**
     * Session context (in browser)
     */
    @Autowired
    protected HttpSession session;

    /**
     * Client Request to Server
     */
    @Autowired
    protected HttpServletRequest request;
    /**
     * Environment config
     */
    @Autowired
    private Environment env;

    /* Auth Trait */

    public void logout()
    {
        logout(session);
    }

    public boolean login(User user)
    {
        return login(session, user);
    }

    public boolean isLoggedIn()
    {
        return isLoggedIn(session);
    }

    public void protectAdmin()
    {
        protectAdmin(session);
    }

    public void protect()
    {
        protect(session);
    }
}
