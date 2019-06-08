package sk.rolandkortvely.cassovia.controllers.traits;

import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import sk.rolandkortvely.cassovia.helpers.Hash;
import sk.rolandkortvely.cassovia.models.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public interface Auth {

    default boolean login(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session, User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return false;
        }
        if (user.getUsername().length() == 0 || user.getPassword().length() == 0) {
            return false;
        }

        User q = User.stream(sessionFactory)
                .filter(u -> user.getUsername().equals(u.getUsername()))
                .findFirst()
                .orElse(null);

        if (q == null) {
            return false;
        }

        if (!Hash.check(user.getPassword(), q.getPassword())) {
            return false;
        }

        session.setAttribute("login", q.getUsername());
        session.setAttribute("auth", Hash.make(q.getPassword()));
        return true;
    }

    default void logout(@NotNull HttpSession session, @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        session.removeAttribute("auth");
        try {
            response.sendRedirect(request.getContextPath() + "/");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown error");
        }
    }

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

        return User.stream(sessionFactory)
                .filter(u -> username.equals(u.getUsername()))
                .filter(u -> Hash.check(u.getPassword(), auth))
                .findFirst().orElse(null);
    }

    default boolean isLoggedIn(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session) {
        return auth(sessionFactory, session) != null;
    }

    default void protect(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session) {
        if (!isLoggedIn(sessionFactory, session)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login please");
        }
    }

    default void protectAdmin(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session) {
        if (!isLoggedIn(sessionFactory, session)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login please");
        }

        if (!auth(sessionFactory, session).getRole().getGroupName().equals("admin")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login please as admin");
        }
    }

    default boolean guestRedirect(@NotNull SessionFactory sessionFactory, @NotNull HttpSession session, @NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        if (isLoggedIn(sessionFactory, session)) {

            try {
                response.sendRedirect(request.getContextPath() + "/");
            } catch (IOException e) {
                return true;
            }
            return true;
        }
        return false;
    }

    default String getClientIp(@NotNull HttpServletRequest request) {
        String remoteAddr = request.getHeader("X-FORWARDED-FOR");
        if (remoteAddr == null || "".equals(remoteAddr)) {
            remoteAddr = request.getRemoteAddr();
        }
        return remoteAddr;
    }
}
