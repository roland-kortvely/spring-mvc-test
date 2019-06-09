package sk.rolandkortvely.cassovia.controllers;

import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ResponseStatusException;
import sk.rolandkortvely.cassovia.models.User;
import sk.rolandkortvely.cassovia.traits.Auth;
import sk.rolandkortvely.cassovia.traits.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
public abstract class AbstractController extends Attributes implements Auth, Session {

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

    /**
     * @return provide error message, if exists to Thymeleaf (DANGER.. red color..)
     */
    @ModelAttribute("error")
    public String error() {
        return getError(session);
    }

    /**
     * @return provide info message, if exists to Thymeleaf (INFO.. blue color..)
     */
    @ModelAttribute("info")
    public String info() {
        return getFlash(session, "info");
    }

    /**
     * @return true or false, whether user is authenticated, for Thymeleaf..
     */
    @ModelAttribute("guest")
    public boolean guest() {
        return !isLoggedIn(sessionFactory, session);
    }

    /**
     * @return true or false, whether authenticated user is an admin, for Thymeleaf..
     */
    @ModelAttribute("admin")
    public boolean admin() {

        if (!isLoggedIn()) {
            return false;
        }

        return auth()
                .getRole()
                .getGroupName().equals("admin");
    }

    /**
     * @return User instance of authenticated user, for Thymeleaf
     */
    @ModelAttribute("auth")
    public User auth() {
        return auth(sessionFactory, session);
    }

    /* NOTHING IMPORTANT BELOW.. JUST A REFLECTION */

    public void redirect(@NotNull HttpServletResponse response, String uri) {
        try {
            response.sendRedirect(request.getContextPath() + uri);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown error");
        }
    }


    /* Session Trait */

    public void error(String msg) {
        error(session, msg);
    }

    public String getFlash(String key) {
        return getFlash(session, key);
    }

    public void flash(String key, String msg) {
        flash(session, key, msg);
    }

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
