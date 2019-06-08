package sk.rolandkortvely.cassovia.controllers;

import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import sk.rolandkortvely.cassovia.controllers.traits.Auth;
import sk.rolandkortvely.cassovia.controllers.traits.Session;
import sk.rolandkortvely.cassovia.models.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
public abstract class AbstractController extends Attributes implements Auth, Session {

    @Autowired
    protected SessionFactory sessionFactory;

    @Autowired
    protected HttpSession session;

    @Autowired
    protected HttpServletRequest request;

    @Autowired
    public JavaMailSender emailSender;

    @ModelAttribute("error")
    public String error() {
        return getError(session);
    }

    @ModelAttribute("info")
    public String info() {
        return getFlash(session, "info");
    }

    @ModelAttribute("guest")
    public boolean guest() {
        return !isLoggedIn(sessionFactory, session, request);
    }

    @ModelAttribute("admin")
    public boolean admin() {

        if (!isLoggedIn()) {
            return false;
        }

        return auth().getRole().getGroupName().equals("admin");
    }

    @ModelAttribute("auth")
    public User auth() {
        return auth(sessionFactory, session, request);
    }

    public void error(String msg) {
        error(session, msg);
    }

    public String getFlash(String key) {
        return getFlash(session, key);
    }

    public void flash(String key, String msg) {
        flash(session, key, msg);
    }

    public boolean login(User user) {
        return login(sessionFactory, session, request, user);
    }

    public void logout(@NotNull HttpServletResponse response) {
        logout(session, request, response);
    }

    public boolean isLoggedIn() {
        return isLoggedIn(sessionFactory, session, request);
    }

    public boolean guestRedirect(@NotNull HttpServletResponse response) {
        return guestRedirect(sessionFactory, session, request, response);
    }

    public void protectAdmin() {
        protectAdmin(sessionFactory, session, request);
    }


    public void protect() {
        protect(sessionFactory, session, request);
    }

    public String getClientIp() {
        return getClientIp(request);
    }
}
