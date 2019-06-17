package sk.rolandkortvely.cassovia.traits;

import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import sk.rolandkortvely.cassovia.helpers.Hash;
import sk.rolandkortvely.cassovia.models.User;

import javax.servlet.http.HttpSession;

/**
 * Auth Trait
 */
public interface Auth {

    /**
     * Login user
     *
     * @param sessionFactory database context (MySQL)
     * @param session        session context (in browser)
     * @param user           POST data from form
     * @return true if user is authorized
     */
    default boolean login(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session, @NotNull User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return false;
        }
        if (user.getUsername().length() == 0 || user.getPassword().length() == 0) {
            return false;
        }

        User q;

        try {

            q = User.query()
                    .where("username", user.getUsername())
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
    default void logout(@NotNull HttpSession session) {
        session.removeAttribute("login");
        session.removeAttribute("auth");
    }

    /**
     * @param sessionFactory database context (MySQL)
     * @param session        session context (in browser)
     * @return authenticated user
     */
    default User auth(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session) {

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
        return User.query()
                .where("username", username)
                .stream()
                .filter(u -> Hash.check(u.getPassword(), auth))
                .findFirst().orElse(null);
    }

    /**
     * @param sessionFactory database context (MySQL)
     * @param session        session context (in browser)
     * @return true if there is an authenticated user
     */
    default boolean isLoggedIn(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session) {
        return auth(sessionFactory, session) != null;
    }

    /**
     * Throw an Exception when user is not authorized to access the given view
     *
     * @param sessionFactory database context (MySQL)
     * @param session        session context (in browser)
     */
    default void protect(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session) {
        if (!isLoggedIn(sessionFactory, session)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login please");
        }
    }

    /**
     * Throw an Exception when user is not authorized to access the given view, user must be Admin
     *
     * @param sessionFactory database context (MySQL)
     * @param session        session context (in browser)
     */
    default void protectAdmin(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session) {
        this.protect(sessionFactory, session);

        if (!auth(sessionFactory, session).isAdmin()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login please as admin");
        }
    }
}
