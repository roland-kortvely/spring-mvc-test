package sk.rolandkortvely.cassovia.controllers;

import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    protected static User authUser = null;

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
        return getInfo(session);
    }

    /**
     * @return true or false, whether user is authenticated, for Thymeleaf..
     */
    @ModelAttribute("guest")
    public boolean guest() {
        return !isLoggedIn(session);
    }

    /**
     * @return true or false, whether authenticated user is an admin, for Thymeleaf..
     */
    @ModelAttribute("admin")
    public boolean admin() {

        if (!isLoggedIn()) {
            return false;
        }

        return auth().isAdmin();
    }

    /**
     * @return User instance of authenticated user, for Thymeleaf
     */
    @ModelAttribute("auth")
    public User auth() {

        if (authUser != null) {
            return authUser;
        }

        authUser = auth(session);

        return authUser;
    }

    public void redirect(@NotNull HttpServletResponse response) {
        redirect(response, "/");
    }

    /**
     * Redirect user to given URL
     *
     * @param response Server Response to Client request
     * @param uri      URL to redirect user to within given domain
     */
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

    public void info(String msg) {
        info(session, msg);
    }

    /* Auth Trait */

    public void logout() {
        logout(session);
    }

    public boolean login(User user) {
        return login(session, user);
    }

    public boolean isLoggedIn() {
        return isLoggedIn(session);
    }

    public void protectAdmin() {
        protectAdmin(session);
    }

    public void protect() {
        protect(session);
    }
}
