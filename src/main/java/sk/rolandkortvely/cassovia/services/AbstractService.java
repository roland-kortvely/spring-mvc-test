package sk.rolandkortvely.cassovia.services;

import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import sk.rolandkortvely.cassovia.models.User;
import sk.rolandkortvely.cassovia.traits.Auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AbstractService implements Auth {

    /**
     * Instance of Mail, connection to SMTP server to send emails
     */
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

    public void logout() {
        logout(session);
    }

    public boolean login(User user) {
        return login(sessionFactory, session, user);
    }

    public boolean isLoggedIn() {
        return isLoggedIn(sessionFactory, session);
    }

    public boolean authenticatedRedirect(@NotNull HttpServletResponse response) {
        return authenticatedRedirect(sessionFactory, session, request, response);
    }

    public void protectAdmin() {
        protectAdmin(sessionFactory, session);
    }

    public void protect() {
        protect(sessionFactory, session);
    }
}
