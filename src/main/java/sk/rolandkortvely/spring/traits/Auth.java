package sk.rolandkortvely.spring.traits;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import sk.rolandkortvely.spring.helpers.Hash;
import sk.rolandkortvely.spring.models.User;

import javax.servlet.http.HttpSession;

/**
 * Auth Trait
 */
public interface Auth
{

    /**
     * Login user
     *
     * @param session session context (in browser)
     * @param user    POST data from form
     * @return true if user is authorized
     */
    default boolean login(@NotNull HttpSession session, @NotNull User user)
    {
        if (user.getUsername() == null || user.getPassword() == null) {
            return false;
        }
        if (user.getUsername().isBlank() || user.getPassword().isBlank()) {
            return false;
        }

        User q;

        try {
            q = new User().where("username", user.getUsername())
                    .stream()
                    .filter(u -> Hash.check(user.getPassword(), u.getPassword()))
                    .findFirst().orElse(null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }

        if (q == null) {
            return false;
        }

        session.setAttribute("login", q.getUsername());
        session.setAttribute("auth", Hash.make(q.getPassword()));
        return true;
    }

    /**
     * Logout authenticated user
     *
     * @param session session context (in browser)
     */
    default void logout(@NotNull HttpSession session)
    {
        session.removeAttribute("login");
        session.removeAttribute("auth");
    }

    /**
     * @param session session context (in browser)
     * @return authenticated user
     */
    default User auth(@NotNull HttpSession session)
    {

        String username = (String) session.getAttribute("login");
        if (username == null) {
            return null;
        }
        if (username.length() == 0) {
            return null;
        }

        String auth = (String) session.getAttribute("auth");
        if (auth == null) {
            return null;
        }
        if (auth.length() == 0) {
            return null;
        }

        /*
         * find authenticated user in database, based on session hashed password and username
         */
        return new User()
                .where("username", username)
                .stream()
                .filter(u -> Hash.check(u.getPassword(), auth))
                .findFirst().orElse(null);
    }

    /**
     * @param session session context (in browser)
     * @return true if there is an authenticated user
     */
    default boolean isLoggedIn(@NotNull HttpSession session)
    {
        return auth(session) != null;
    }

    /**
     * Throw an Exception when user is not authorized to access the given view
     *
     * @param session session context (in browser)
     */
    default void protect(@NotNull HttpSession session)
    {
        if (!isLoggedIn(session)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login please");
        }
    }

    /**
     * Throw an Exception when user is not authorized to access the given view, user must be Admin
     *
     * @param session session context (in browser)
     */
    default void protectAdmin(@NotNull HttpSession session)
    {
        this.protect(session);

        if (!auth(session).isAdmin()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login please as admin");
        }
    }
}
